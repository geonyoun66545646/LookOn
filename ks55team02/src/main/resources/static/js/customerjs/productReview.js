(function() {
	let productId; // 상품 ID를 저장할 전역 변수

	/**
	 * [Public] 외부(HTML)에서 호출할 초기화 함수
	 * @param {string} pId - 상품 ID
	 * @param {object} initialPaginationData - 서버에서 받은 첫 페이지의 페이지네이션 데이터
	 */
	window.initReviewSection = function(pId, initialPaginationData) {
		productId = pId;
		const reviewTabLink = document.getElementById('product-review-link');

		if (reviewTabLink) {
			reviewTabLink.addEventListener('click', function() {
				const reviewSection = document.getElementById('review-section-content');
				if (reviewSection && reviewSection.children.length === 0) {
					renderReviewPage(initialPaginationData);
				}
			}, { once: true });
		}
	};

	/**
	 * 서버로부터 특정 페이지의 리뷰 데이터를 비동기적으로 가져와 렌더링하는 함수
	 * @param {number} page - 요청할 페이지 번호
	 */
	async function fetchAndRenderReviews(page) {
		try {
			// [수정] 컨트롤러와 일치시킨 API 경로
			const response = await fetch(`/api/reviews/products/${productId}/reviews?page=${page}`);
			
			if (!response.ok) {
				throw new Error('리뷰 데이터를 불러오는 데 실패했습니다: ' + response.status);
			}
			const paginationData = await response.json();
			renderReviewPage(paginationData);

		} catch (error) {
			console.error('Error fetching reviews:', error);
			const reviewSection = document.getElementById('review-section-content');
			if (reviewSection) {
				reviewSection.innerHTML = '<p class="text-center text-danger">리뷰를 불러오는 중 오류가 발생했습니다.</p>';
			}
		}
	}

	/**
	 * 수신된 pagination 객체를 기반으로 페이지 전체를 다시 렌더링하는 함수
	 * @param {object} pagination - 서버로부터 받은 페이지네이션 객체
	 */
	function renderReviewPage(pagination) {
		const reviewSection = document.getElementById('review-section-content');
		if (!reviewSection || !pagination) return;

		const reviewsHtml = generateReviewsHtml(pagination.list);
		const paginationHtml = generatePaginationHtml(pagination);

		reviewSection.innerHTML = `
			<h5>리뷰 (<span>${pagination.totalCount}</span>)</h5>
			<hr class="mt-2 mb-4">
			<div id="review-list-container">${reviewsHtml}</div>
			<nav id="pagination-container" class="pagination-container d-flex justify-content-center">${paginationHtml}</nav>
		`;

		// ★ 새로 생성된 HTML 요소들에 이벤트 리스너를 다시 연결
		attachPaginationEvents();
		applyDragScrollToSliders(reviewSection);
		attachLikeButtonEvents(reviewSection); // '좋아요' 버튼 이벤트 연결
	}

	/**
	 * 리뷰 데이터 배열을 기반으로 리뷰 목록 HTML을 생성하는 함수
	 */
	function generateReviewsHtml(reviewList) {
		if (!reviewList || reviewList.length === 0) {
			return '<p class="text-center py-5">등록된 리뷰가 없습니다.</p>';
		}

		return reviewList.map((review, index) => {
			const profileImg = review.userProfile?.prflImg || '/uploads/profiles/defaultprofile.jpg';
			const nickname = review.user?.userNcnm || '익명';
			const writtenDate = review.wrtYmd ? new Date(review.wrtYmd).toLocaleDateString('ko-KR', { year: '2-digit', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').slice(0, -1) : '';
			const ratingWidth = (review.evlScr || 0) * 20;
			const likeCount = review.helpdCnt ?? 0;
			const likedClass = review.isLiked ? 'active' : '';

			const imagesHtml = (review.reviewImages && review.reviewImages.length > 0) ? `
				<div class="review-image-slider mb-3">
					<div class="review-image-track">
						${review.reviewImages.map(img => 
							(img.storeImage && img.storeImage.imgAddr) ? `
							<div class="review-image-item">
								<img src="${img.storeImage.imgAddr}" alt="리뷰 이미지"/>
							</div>` : ''
						).join('')}
					</div>
				</div>
			` : '';

			return `
				<div class="review ${index < reviewList.length - 1 ? 'review-item-border' : ''}">
					<div class="review-header d-flex align-items-center mb-1">
						<div class="profile-image-wrapper mr-3">
							<img src="${profileImg}" alt="프로필" class="rounded-circle">
						</div>
						<div class="review-meta-info w-100">
							<div class="d-flex align-items-baseline">
								<h5 class="mb-0"><a href="#">${nickname}</a></h5>
								<span class="review-date text-muted ml-2">${writtenDate}</span>
							</div>
							<div class="review-meta">
								<div class="ratings-container">
									<div class="ratings"><div class="ratings-val" style="width: ${ratingWidth}%;"></div></div>
									<span class="ratings-text ml-1">${review.evlScr || 0}</span>
								</div>
							</div>
						</div>
					</div>
					<div class="review-body">
						${imagesHtml}
						<div class="review-content mb-3"><p>${review.reviewCn || ''}</p></div>
						<div class="review-footer">
							<button class="btn btn-sm btn-outline-primary btn-helpful ${likedClass}" data-review-id="${review.reviewId}">
								<i class="far fa-thumbs-up"></i> 도움이 돼요 
								<span class="helpful-count">${likeCount}</span>
							</button>
						</div>
					</div>
				</div>
			`;
		}).join('');
	}
	
	/**
	 * 페이지네이션 HTML을 생성하는 함수
	 */
	function generatePaginationHtml(pagination) {
		if (!pagination || pagination.totalPages <= 1) return '';

		const { currentPage, totalPages, startPage, endPage } = pagination;
		let html = '<ul class="pagination">';

		html += currentPage > 1 ? `<li class="page-item"><a class="page-link" href="#" data-page="1">처음</a></li>` : `<li class="page-item disabled"><a class="page-link">처음</a></li>`;
		html += currentPage > 1 ? `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage - 1}">이전</a></li>` : `<li class="page-item disabled"><a class="page-link">이전</a></li>`;
		for (let i = startPage; i <= endPage; i++) {
			html += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
		}
		html += currentPage < totalPages ? `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage + 1}">다음</a></li>` : `<li class="page-item disabled"><a class="page-link">다음</a></li>`;
		html += currentPage < totalPages ? `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">마지막</a></li>` : `<li class="page-item disabled"><a class="page-link">마지막</a></li>`;
		html += '</ul>';
		return html;
	}

	/**
	 * 페이지네이션 컨테이너에 클릭 이벤트를 연결하는 함수
	 */
	function attachPaginationEvents() {
		const paginationContainer = document.getElementById('pagination-container');
		if (paginationContainer) {
			paginationContainer.addEventListener('click', function(event) {
				event.preventDefault();
				const target = event.target.closest('a.page-link');
				if (target && target.dataset.page) {
					fetchAndRenderReviews(parseInt(target.dataset.page, 10));
				}
			});
		}
	}

	/**
	 * [신규] '좋아요' 버튼에 대한 이벤트 리스너를 설정하는 함수
	 */
	function attachLikeButtonEvents(container) {
		container.addEventListener('click', function(event) {
			const likeButton = event.target.closest('.btn-helpful');
			if (likeButton) {
				event.preventDefault();
				const reviewId = likeButton.dataset.reviewId;
				if (reviewId) {
					handleLikeClick(reviewId, likeButton);
				}
			}
		});
	}

	/**
	 * [신규] '좋아요' 클릭을 실제 처리하고 서버와 통신하는 함수
	 */
	async function handleLikeClick(reviewId, buttonElement) {
		try {
			const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
			const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
			
			const headers = { 'Content-Type': 'application/json' };
			if (csrfToken && csrfHeader) {
				headers[csrfHeader] = csrfToken;
			}

			// [수정] 컨트롤러와 일치시킨 API 경로
			const response = await fetch(`/api/reviews/${reviewId}/like`, {
				method: 'POST',
				headers: headers
			});

			if (!response.ok) {
				const errorData = await response.json();
				alert(errorData.message || '요청을 처리할 수 없습니다.');
				throw new Error(errorData.message || 'Like request failed');
			}

			const result = await response.json();

			// UI 업데이트
			buttonElement.querySelector('.helpful-count').textContent = result.likeCount;
			buttonElement.classList.toggle('active', result.liked);

		} catch (error) {
			console.error('Like button click error:', error);
		}
	}

	/**
	 * 이미지 슬라이더에 마우스 드래그 스크롤 기능을 적용하는 함수
	 */
	function applyDragScrollToSliders(container) {
		const sliders = container.querySelectorAll('.review-image-slider');
		sliders.forEach(slider => {
			let isDown = false, startX, scrollLeft;
			slider.addEventListener('mousedown', e => {
				isDown = true;
				slider.classList.add('active');
				startX = e.pageX - slider.offsetLeft;
				scrollLeft = slider.scrollLeft;
			});
			['mouseleave', 'mouseup'].forEach(event => slider.addEventListener(event, () => {
				isDown = false;
				slider.classList.remove('active');
			}));
			slider.addEventListener('mousemove', e => {
				if (!isDown) return;
				e.preventDefault();
				const x = e.pageX - slider.offsetLeft;
				slider.scrollLeft = scrollLeft - (x - startX) * 2;
			});
		});
	}
})();
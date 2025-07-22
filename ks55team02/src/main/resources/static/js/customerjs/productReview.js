(function() {
    let productId; // 상품 ID를 저장할 변수

    /**
     * [Public] HTML에서 호출할 초기화 함수
     * @param {string} pId - 상품 ID
     * @param {object} initialPaginationData - 서버에서 받은 첫 페이지의 페이지네이션 데이터
     */
    window.initReviewSection = function(pId, initialPaginationData) {
        productId = pId;
        const reviewTabLink = document.getElementById('product-review-link');

        if (reviewTabLink) {
            // 처음 탭을 클릭했을 때만 첫 페이지 리뷰를 렌더링
            reviewTabLink.addEventListener('click', function() {
                const reviewSection = document.getElementById('review-section-content');
                // 리뷰가 아직 렌더링되지 않았을 경우에만 실행
                if (reviewSection && reviewSection.children.length === 0) {
                    renderReviewPage(initialPaginationData);
                }
            }, { once: true }); // 이벤트가 한 번만 실행되도록 { once: true } 옵션 추가
        }
    };

    /**
     * 서버로부터 특정 페이지의 리뷰 데이터를 가져와 렌더링하는 함수
     * @param {number} page - 요청할 페이지 번호
     */
    async function fetchAndRenderReviews(page) {
        try {
            const response = await fetch(`/customer/review/api/products/${productId}/reviews?page=${page}`);
            
            if (!response.ok) {
                throw new Error('리뷰 데이터를 불러오는 데 실패했습니다. 상태 코드: ' + response.status);
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
     * [수정됨] 수신된 pagination 객체를 기반으로 페이지 전체를 다시 렌더링하는 함수
     * @param {object} pagination - 서버로부터 받은 페이지네이션 객체
     */
    function renderReviewPage(pagination) {
        const reviewSection = document.getElementById('review-section-content');
        if (!reviewSection || !pagination) return;

        // [1] 리뷰 목록과 페이지네이션 HTML 생성
        const reviewsHtml = generateReviewsHtml(pagination.list);
        const paginationHtml = generatePaginationHtml(pagination);

        // [2] 생성된 HTML로 리뷰 섹션의 내용을 완전히 교체 (기존 이벤트 리스너 모두 사라짐)
        reviewSection.innerHTML = `
            <h3>리뷰 (<span>${pagination.totalCount}</span>)</h3>
            <hr class="mt-2 mb-4">
            <div id="review-list-container">${reviewsHtml}</div>
            <nav id="pagination-container" class="pagination-container d-flex justify-content-center">${paginationHtml}</nav>
        `;

        // [3] ★★★ 새로 생성된 요소들에 이벤트 리스너를 다시 연결 ★★★
        attachPaginationEvents();          // 페이지 번호 버튼에 클릭 이벤트 다시 추가
        applyDragScrollToSliders(reviewSection); // 이미지 슬라이더에 드래그 스크롤 기능 다시 추가
    }

    /**
     * 리뷰 데이터 배열을 기반으로 리뷰 목록 HTML을 생성하는 함수
     * @param {Array} reviewList - 리뷰 객체 배열
     * @returns {string} - 생성된 HTML 문자열
     */
    function generateReviewsHtml(reviewList) {
        if (!reviewList || reviewList.length === 0) {
            return '<p class="text-center py-3">등록된 리뷰가 없습니다.</p>';
        }

        return reviewList.map((review, index) => {
            const profileImg = review.userProfile?.prflImg || '/uploads/profiles/defaultprofile.jpg';
            const nickname = review.user?.userNcnm || '익명';
            const writtenDate = review.wrtYmd ? new Date(review.wrtYmd).toLocaleDateString('ko-KR', { year: '2-digit', month: '2-digit', day: '2-digit' }).replace(/\. /g, '.').slice(0, -1) : '';
            const ratingWidth = (review.evlScr || 0) * 20;

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
            
            const likedClass = review.isLiked ? 'active' : ''; // isLiked 상태는 페이지 이동마다 초기화됨

            return `
                <div class="review ${index < reviewList.length - 1 ? 'review-item-border' : ''}">
                    <div class="review-header d-flex align-items-center mb-1">
                        <div class="profile-image-wrapper mr-3">
                            <img src="${profileImg}" alt="프로필" class="rounded-circle">
                        </div>
                        <div class="review-meta-info w-100">
                            <div class="d-flex align-items-baseline">
                                <h5 class="mb-0"><a href="#">${nickname}</a></h5>
                                <span class="review-date text-muted ml-2" style="font-size: 0.85em;">${writtenDate}</span>
                            </div>
                            <div class="review-meta">
                                <div class="ratings-container">
                                    <div class="ratings">
                                        <div class="ratings-val" style="width: ${ratingWidth}%;"></div>
                                    </div>
                                    <span class="ratings-text ml-1">${review.evlScr || 0}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="review-body">
                        ${imagesHtml}
                        <div class="review-content mb-3">
                            <p>${review.reviewCn || ''}</p>
                        </div>
                        <div class="review-footer">
                            <button class="btn btn-sm btn-outline-primary btn-helpful ${likedClass}" data-review-id="${review.reviewId || review.gdsEvlNo}">
                                <i class="far fa-thumbs-up"></i> 도움이 돼요 
                                <span class="helpful-count">${review.helpfulCount || 0}</span>
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }
    
    /**
     * CustomerPagination 객체를 받아 페이지네이션 HTML을 생성하는 함수
     * @param {object} pagination - 페이지네이션 데이터 객체
     * @returns {string} - 생성된 HTML 문자열
     */
	function generatePaginationHtml(pagination) {
	    if (!pagination || pagination.totalPages <= 1) return '';

	    // 필요한 값만 구조분해할당으로 가져옵니다.
	    const { currentPage, totalPages, startPage, endPage } = pagination;
	    let html = '<ul class="pagination">';

	    // --- '처음' 버튼 로직 (currentPage 기준) ---
	    if (currentPage > 1) {
	        html += `<li class="page-item"><a class="page-link" href="#" data-page="1">처음</a></li>`;
	    } else {
	        html += `<li class="page-item disabled"><a class="page-link" href="#" tabindex="-1">처음</a></li>`;
	    }

	    // --- '이전' 버튼 로직 (currentPage 기준) ---
	    if (currentPage > 1) {
	        // 링크는 이전 페이지(currentPage - 1)로 설정합니다.
	        html += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage - 1}">이전</a></li>`;
	    } else {
	        html += `<li class="page-item disabled"><a class="page-link" href="#" tabindex="-1">이전</a></li>`;
	    }

	    // --- 페이지 번호 로직 (기존과 동일) ---
	    for (let i = startPage; i <= endPage; i++) {
	        html += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
	    }

	    // --- '다음' 버튼 로직 (currentPage, totalPages 기준) ---
	    if (currentPage < totalPages) {
	        // 링크는 다음 페이지(currentPage + 1)로 설정합니다.
	        html += `<li class="page-item"><a class="page-link" href="#" data-page="${currentPage + 1}">다음</a></li>`;
	    } else {
	        html += `<li class="page-item disabled"><a class="page-link" href="#" tabindex="-1">다음</a></li>`;
	    }

	    // --- '마지막' 버튼 로직 (currentPage, totalPages 기준) ---
	    if (currentPage < totalPages) {
	        html += `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">마지막</a></li>`;
	    } else {
	        html += `<li class="page-item disabled"><a class="page-link" href="#" tabindex="-1">마지막</a></li>`;
	    }

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
                    const page = parseInt(target.dataset.page, 10);
                    fetchAndRenderReviews(page);
                }
            });
        }
    }

    /**
     * 컨테이너 내의 모든 이미지 슬라이더에 마우스 드래그 스크롤 기능을 적용하는 함수
     * @param {HTMLElement} container - 이벤트를 적용할 최상위 컨테이너 요소
     */
    function applyDragScrollToSliders(container) {
        const sliders = container.querySelectorAll('.review-image-slider');

        sliders.forEach(slider => {
            let isDown = false;
            let startX;
            let scrollLeft;

            slider.addEventListener('mousedown', (e) => {
                e.preventDefault();
                isDown = true;
                slider.classList.add('active');
                startX = e.pageX - slider.offsetLeft;
                scrollLeft = slider.scrollLeft;
            });

            slider.addEventListener('mouseleave', () => {
                isDown = false;
                slider.classList.remove('active');
            });

            slider.addEventListener('mouseup', () => {
                isDown = false;
                slider.classList.remove('active');
            });

            slider.addEventListener('mousemove', (e) => {
                if (!isDown) return;
                e.preventDefault();
                const x = e.pageX - slider.offsetLeft;
                const walk = (x - startX) * 2; // 드래그 감도 조절
                slider.scrollLeft = scrollLeft - walk;
            });
        });
    }

})();
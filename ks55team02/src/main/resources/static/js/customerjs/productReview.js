// static/js/customerjs/productReview.js

// 전역 변수 충돌 방지를 위한 즉시 실행 함수
(function() {
    let allReviews = [];
    let reviewsRendered = false;
    const REVIEWS_PAGE_SIZE = 5;

    // HTML에서 호출할 초기화 함수
    window.initReviewSection = function(reviewData) {
        allReviews = reviewData.map((review, index) => ({
            ...review,
            id: review.reviewId || review.gdsEvlNo || `temp-id-${index}`, 
            helpfulCount: review.helpfulCount || Math.floor(Math.random() * 20),
            isLiked: false
        })) || [];
        
        const reviewTabLink = document.getElementById('product-review-link');
        if (reviewTabLink) {
            reviewTabLink.addEventListener('click', function() {
                if (!reviewsRendered) {
                    renderReviewPage(1);
                    reviewsRendered = true;
                }
            });
        }
    };

    // 특정 페이지를 렌더링하는 메인 함수
    function renderReviewPage(page) {
        const reviewSection = document.getElementById('review-section-content');
        if (!reviewSection) return;

        const totalCount = allReviews.length;
        const totalPages = Math.ceil(totalCount / REVIEWS_PAGE_SIZE);
        const startIndex = (page - 1) * REVIEWS_PAGE_SIZE;
        const endIndex = startIndex + REVIEWS_PAGE_SIZE;
        const reviewsForPage = allReviews.slice(startIndex, endIndex);

        const reviewsHtml = generateReviewsHtml(reviewsForPage);
        const paginationHtml = generatePaginationHtml(page, totalPages);

        reviewSection.innerHTML = `
            <h3>리뷰 (<span>${totalCount}</span>)</h3>
            <hr class="mt-2 mb-4">
            <div id="review-list-container">${reviewsHtml}</div>
            <nav id="pagination-container" class="pagination-container">${paginationHtml}</nav>
        `;

        attachPaginationEvents();
        applyDragScrollToSliders(reviewSection);
        attachHelpfulButtonEvents(reviewSection);
    }

    // [수정됨] 리뷰 목록 HTML을 생성하는 함수
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
            
            const likedClass = review.isLiked ? 'active' : '';

            // [수정] 아래 return 문의 HTML 구조가 변경되었습니다.
            return `
                <div class="review ${index < reviewList.length - 1 ? 'review-item-border' : ''}">
                    <div class="review-header d-flex align-items-center mb-1">
                        <div class="profile-image-wrapper mr-3"><img src="${profileImg}" alt="프로필" class="rounded-circle"></div>
                        <div class="review-meta-info w-100">
                            
                            <div class="d-flex align-items-baseline">
                                <h5 class="mb-0"><a href="#">${nickname}</a></h5>
                                <span class="review-date text-muted ml-2" style="font-size: 0.85em;">${writtenDate}</span>
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
                            <button class="btn btn-sm btn-outline-primary btn-helpful ${likedClass}" data-review-id="${review.id}">
                                <i class="far fa-thumbs-up"></i> 도움이 돼요 <span class="helpful-count">${review.helpfulCount}</span>
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }
    
    // 페이지네이션 UI HTML을 생성하는 함수 (변경 없음)
    function generatePaginationHtml(currentPage, totalPages) {
        if (totalPages <= 1) return '';
        const blockSize = 5;
        const startPage = Math.floor((currentPage - 1) / blockSize) * blockSize + 1;
        const endPage = Math.min(startPage + blockSize - 1, totalPages);
        let html = '<ul class="pagination">';
        if (startPage > 1) html += `<li class="page-item"><a class="page-link" href="#" data-page="${startPage - 1}">«</a></li>`;
        for (let i = startPage; i <= endPage; i++) html += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
        if (endPage < totalPages) html += `<li class="page-item"><a class="page-link" href="#" data-page="${endPage + 1}">»</a></li>`;
        html += '</ul>';
        return html;
    }

    // 페이지네이션 링크에 클릭 이벤트를 거는 함수 (변경 없음)
    function attachPaginationEvents() {
        const paginationContainer = document.getElementById('pagination-container');
        if (paginationContainer) {
            paginationContainer.addEventListener('click', function(event) {
                event.preventDefault();
                const target = event.target.closest('a.page-link');
                if (target && target.dataset.page) {
                    renderReviewPage(parseInt(target.dataset.page, 10));
                }
            });
        }
    }

    // '도움이 돼요' 버튼에 대한 이벤트 처리 함수 (변경 없음)
    function attachHelpfulButtonEvents(container) {
        container.addEventListener('click', function(event) {
            const button = event.target.closest('.btn-helpful');
            if (!button) return;

            event.preventDefault();

            const reviewId = button.dataset.reviewId;
            const review = allReviews.find(r => r.id == reviewId);
            if (!review) return;

            const countSpan = button.querySelector('.helpful-count');

            if (review.isLiked) {
                review.isLiked = false;
                review.helpfulCount--;
                button.classList.remove('active');
            } else {
                review.isLiked = true;
                review.helpfulCount++;
                button.classList.add('active');
            }
            
            countSpan.textContent = review.helpfulCount;

            // TODO: 실제 서버와 통신하는 AJAX/fetch 로직 추가
            console.log(`Review ID ${reviewId}의 좋아요 상태: ${review.isLiked}`);
        });
    }

    // 드래그 스크롤 기능 적용 함수 (변경 없음)
    function applyDragScrollToSliders(container) {
        const sliders = container.querySelectorAll('.review-image-slider');
        sliders.forEach(slider => {
            let isDown = false, startX, scrollLeft;
            slider.addEventListener('mousedown', e => { isDown = true; slider.classList.add('active'); startX = e.pageX - slider.offsetLeft; scrollLeft = slider.scrollLeft; });
            slider.addEventListener('mouseleave', () => { isDown = false; slider.classList.remove('active'); });
            slider.addEventListener('mouseup', () => { isDown = false; slider.classList.remove('active'); });
            slider.addEventListener('mousemove', e => { if (!isDown) return; e.preventDefault(); const x = e.pageX - slider.offsetLeft; const walk = (x - startX) * 2; slider.scrollLeft = scrollLeft - walk; });
        });
    }

})();
// static/js/customerjs/productReview.js

// 전역 변수 충돌 방지를 위한 즉시 실행 함수
(function() {
    let allReviews = [];
    let reviewsRendered = false;
    const REVIEWS_PAGE_SIZE = 5;

    // HTML에서 호출할 초기화 함수
    window.initReviewSection = function(reviewData) {
        allReviews = reviewData || [];
        
        const reviewTabLink = document.getElementById('product-review-link');
        if (reviewTabLink) {
            // [핵심 수정] jQuery 없이 순수 JavaScript의 click 이벤트 리스너를 사용합니다.
            reviewTabLink.addEventListener('click', function() {
                // 이미 렌더링되었다면 다시 작업하지 않도록 플래그로 제어합니다.
                if (!reviewsRendered) {
                    renderReviewPage(1); // 1페이지부터 렌더링 시작
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
        // slice()는 endIndex 바로 앞까지 자르므로, endIndex 계산은 그대로 둡니다.
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
    }

    // 리뷰 목록 HTML을 생성하는 함수 (변경 없음)
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

            return `
                <div class="review ${index < reviewList.length - 1 ? 'review-item-border' : ''}">
                    <div class="review-header d-flex align-items-center mb-3">
                        <div class="profile-image-wrapper mr-3"><img src="${profileImg}" alt="프로필" class="rounded-circle"></div>
                        <div class="review-meta-info w-100">
                            <div class="d-flex justify-content-between align-items-center">
                                <h5 class="mb-0"><a href="#">${nickname}</a></h5>
                                <span class="review-date">${writtenDate}</span>
                            </div>
                            <div class="review-meta mt-1">
                                <div class="ratings-container">
                                    <div class="ratings"><div class="ratings-val" style="width: ${ratingWidth}%;"></div></div>
                                    <span class="ratings-text ml-1">${review.evlScr || 0}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="review-body">
                        ${imagesHtml}
                        <div class="review-content"><p>${review.reviewCn || ''}</p></div>
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

    // [수정] renderReviewPage 함수에서 endIndex 계산 오류 수정
    function renderReviewPage(page) {
        const reviewSection = document.getElementById('review-section-content');
        if (!reviewSection) return;
    
        const totalCount = allReviews.length;
        const totalPages = Math.ceil(totalCount / REVIEWS_PAGE_SIZE);
        const startIndex = (page - 1) * REVIEWS_PAGE_SIZE;
        // slice()는 endIndex를 포함하지 않으므로, 계산은 그대로 둡니다.
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
    }

})();
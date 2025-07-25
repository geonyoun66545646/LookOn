// /js/customerjs/feedListByUserNo.js (최종 수정본)

$(document).ready(function() {
    // 1. 페이지 상태를 관리하는 변수들을 초기화합니다.
    const container = $('.my-feed-container');
    const pageOwnerUserNo = container.data('page-owner-user-no');
    const loginUserNo = container.data('login-user-no');
    
    let currentPage = 1;
    let hasNext = true; // 다음 페이지가 있다고 가정하고 시작
    let isLoading = false;

    // 이 페이지가 '내 피드'인지 '다른 사용자 피드'인지에 따라 API 주소를 결정합니다.
    const isMyFeed = (pageOwnerUserNo === loginUserNo);
    const apiUrl = isMyFeed ? '/customer/api/feeds/my-feed' : `/customer/api/feeds/user-feed/${pageOwnerUserNo}`;
    
    // 로딩 인디케이터 DOM 요소를 변수로 캐싱하여 성능을 향상시킵니다.
    const $loadingIndicator = $('#loading-indicator');

    /**
     * 2. 모든 피드 데이터 요청을 처리하는 중앙 함수입니다.
     * @param {number} page - 요청할 페이지 번호
     * @param {boolean} replace - true일 경우 기존 피드 목록을 지우고 새로 채웁니다. (정렬 변경 시)
     *                            false일 경우 기존 목록에 추가합니다. (초기 로딩, 무한 스크롤 시)
     */
    function loadFeeds(page, replace = false) {
        if (isLoading || !hasNext) return;
        isLoading = true;
        $loadingIndicator.show();

        const sortOrder = $('#sort-dropdown').val();

        $.ajax({
            url: apiUrl,
            type: 'GET',
            data: {
                page: page,
                sort: sortOrder
            },
            dataType: 'json'
        })
        .done(function(response) {
            // 정렬 변경 시, 기존 그리드 내용을 모두 비웁니다.
            if (replace) {
                $('#feed-grid-container').empty();
            }
            
            if (response.feedList && response.feedList.length > 0) {
                renderFeeds(response.feedList); // 피드 아이템을 화면에 렌더링합니다.
                hasNext = response.hasNext; // 다음 페이지 존재 여부를 갱신합니다.
                currentPage = page; // 현재 페이지 번호를 갱신합니다.
            } else {
                hasNext = false; // 더 이상 가져올 데이터가 없습니다.
                if (replace || page === 1) { // 첫 페이지 로드이거나 정렬 변경 시 데이터가 없는 경우
                    const message = isMyFeed ? '작성한 피드가 없습니다.' : '이 사용자가 작성한 피드가 없습니다.';
                    $('#feed-grid-container').html(`<div class="no-feeds-container"><p>${message}</p></div>`);
                }
            }
        })
        .fail(function(xhr) {
            console.error("피드를 불러오는데 실패했습니다.", xhr.responseText);
            hasNext = false; // 실패 시 더 이상 시도하지 않도록 설정
        })
        .always(function() {
            isLoading = false;
            // 로딩이 끝나면, 다음 페이지가 있을 때만 인디케이터를 남겨두고 없으면 숨깁니다.
            if (!hasNext) {
                $loadingIndicator.hide();
            }
        });
    }

    /**
     * 3. 서버에서 받은 피드 목록(JSON)을 HTML로 변환하여 DOM에 추가하는 함수입니다.
     *    (CSR 원칙 및 HTML 생성 일관성 규칙 준수)
     * @param {Array} feedList - 피드 데이터 배열
     */
    function renderFeeds(feedList) {
        let feedItemsHtml = '';
        feedList.forEach(feed => {
            const defaultImageUrl = '/images/default-feed.png';
            const imageUrl = feed.representativeImage?.imgFilePathNm || defaultImageUrl;
            const imageAlt = feed.representativeImage?.imgAltTxtCn || '피드 대표 이미지';

            let detailLink;
            if (isMyFeed) {
                detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=my`;
            } else {
                detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=user&userNo=${pageOwnerUserNo}`;
            }

            feedItemsHtml += `
                <article class="feed-item">
                    <a href="${detailLink}">
                        <img src="${imageUrl}" alt="${imageAlt}">
                        <div class="item-overlay">
                            <span class="likes">♥ ${feed.likeCount}</span>
                        </div>
                    </a>
                </article>`;
        });
        $('#feed-grid-container').append(feedItemsHtml);
    }
    
    // 4. 이벤트 핸들러: 정렬 드롭다운 변경
    $('#sort-dropdown').on('change', function() {
        // 페이지 새로고침 대신, 비동기적으로 첫 페이지 데이터를 다시 로드합니다.
        hasNext = true; // 정렬 시 다시 로드해야 하므로 hasNext를 초기화합니다.
        loadFeeds(1, true);
    });

    // 5. IntersectionObserver를 사용한 효율적인 무한 스크롤 구현
    const observerTarget = document.getElementById('loading-indicator');
    if (observerTarget) {
        const observer = new IntersectionObserver((entries) => {
            // 감시 대상이 화면에 보이고, 로딩 중이 아니며, 다음 페이지가 있을 경우에만 다음 페이지를 로드합니다.
            if (entries[0].isIntersecting && !isLoading && hasNext) {
                loadFeeds(currentPage + 1, false);
            }
        }, { threshold: 0.1 });
        
        observer.observe(observerTarget);
    }
    
    // 6. 페이지 최초 진입 시, 첫 페이지 데이터를 로드합니다.
    loadFeeds(1, false);
});
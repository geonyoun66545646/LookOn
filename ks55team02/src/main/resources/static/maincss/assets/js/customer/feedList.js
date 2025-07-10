/**
 * feedList.js
 * 피드 목록 페이지의 동적 기능 (무한 스크롤) 처리
 */
$(() => {

    // --- 상수 및 변수 선언 ---
    const $feedGrid = $('.feed-grid');
    const $loadingIndicator = $('#loading-indicator');
    
    // 피드 그리드가 없는 페이지에서는 스크립트를 실행하지 않음
    if (!$feedGrid.length) {
        return;
    }

    let currentPage = 2; // 다음으로 요청할 페이지 번호 (1페이지는 이미 로드됨)
    let isLoading = false; // 중복 요청 방지를 위한 플래그
    let hasNext = /*[[${hasNext}]]*/ true; // 서버로부터 전달받은 다음 페이지 존재 여부


    // --- 이벤트 핸들러 ---
    $(window).on('scroll', () => {
        // 스크롤이 맨 아래 근처에 도달했고, 로딩 중이 아니며, 다음 페이지가 있을 경우에만 함수 호출
        if ($(window).scrollTop() + $(window).height() > $(document).height() - 300 && !isLoading && hasNext) {
            loadMoreFeeds();
        }
    });


    // --- 함수 정의 ---
    /**
     * Ajax를 통해 다음 페이지의 피드 데이터를 불러와 그리드에 추가하는 함수
     */
    function loadMoreFeeds() {
        isLoading = true;
        $loadingIndicator.show();

        $.ajax({
            url: '/api/feed/list',
            type: 'GET',
            data: { page: currentPage },
            dataType: 'json'
        }).done(response => {
            // 요청 성공 시
            if (response.feedList && response.feedList.length > 0) {
                let newItemsHtml = '';
                response.feedList.forEach(feed => {
                    const imageUrl = (feed.representativeImage && feed.representativeImage.imgFilePathNm) 
                                     ? feed.representativeImage.imgFilePathNm 
                                     : '/images/default-feed.png'; // 기본 이미지 경로
                    
                    const writerNickname = (feed.writerInfo && feed.writerInfo.userNcnm) 
                                           ? feed.writerInfo.userNcnm 
                                           : '알 수 없는 사용자';
                    
                    newItemsHtml += `
                        <article class="feed-item">
                            <a href="/customer/feed/feedDetail?feedSn=${feed.feedSn}">
                                <img src="${imageUrl}" alt="피드 대표 이미지">
                                <div class="item-overlay">
                                    <span class="likes">♥ ${feed.likeCount}</span>
                                </div>
                            </a>
                            <div class="feed-writer-info">
                                <span>${writerNickname}</span>
                            </div>
                        </article>
                    `;
                });

                $feedGrid.append(newItemsHtml); // 생성된 HTML을 그리드에 추가
                currentPage++; // 다음 요청을 위해 페이지 번호 증가
            }

            hasNext = response.hasNext; // 다음 페이지 존재 여부 업데이트

        }).fail(error => {
            // 요청 실패 시
            console.error("피드 로딩 실패:", error);
            $loadingIndicator.html('피드를 불러오는 데 실패했습니다.');
            hasNext = false; // 실패 시 더 이상 요청하지 않도록 설정

        }).always(() => {
            // 성공/실패 여부와 관계 없이 항상 실행 (complete 콜백과 동일한 역할)
            isLoading = false;
            if (!hasNext) {
                // 더 이상 불러올 데이터가 없으면 로딩 인디케이터를 완전히 숨김
                $loadingIndicator.hide();
            } else {
                // 아직 불러올 데이터가 있으면 다음 스크롤을 위해 숨김
                $loadingIndicator.hide();
            }
        });
    }

});
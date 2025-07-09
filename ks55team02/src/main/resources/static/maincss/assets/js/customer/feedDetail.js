/**
 * feedDetail.js
 * 피드 상세 페이지의 동적 기능 (무한 스크롤) 처리
 */
/* <![CDATA[ */
$(() => {
    // --- 상수 및 변수 선언 ---
    const $feedContainer = $('#feed-container');
    const $loadingIndicator = $('#loading-indicator');
    
    // [변경] wrtrUserNo는 다음 피드를 로드할 때마다 마지막 피드에서 가져오므로, 여기서 미리 선언할 필요가 없습니다.
    
    if (!$feedContainer.length) return;

    let isLoading = false;
    let hasNext = true; // 다음 피드가 있을 것으로 가정하고 시작

    // --- 함수 정의 (이 부분은 변경 없음) ---
    function loadNextFeed() {
        // [변경] 함수가 호출될 때마다 마지막 피드를 기준으로 정보를 가져오도록 수정
        const $lastPost = $feedContainer.children('.feed-post:last');
        if ($lastPost.length === 0) return;

        const currentFeedCrtDt = $lastPost.data('crt-dt');
        const wrtrUserNo = $lastPost.data('wrtr-user-no');

        // 이미 로딩 중이거나 다음 페이지가 없으면 중단
        if (isLoading || !hasNext) return;

        isLoading = true;
        $loadingIndicator.show();

        $.ajax({
            url: '/customer/feed/next',
            type: 'GET',
            data: { 
                currentFeedCrtDt: currentFeedCrtDt,
                wrtrUserNo: wrtrUserNo
            },
            dataType: 'json'
        }).done(nextFeed => {
            if (!nextFeed) {
                hasNext = false;
                // [변경] 다음 피드가 없으면 scroll 이벤트 리스너를 제거하여 불필요한 연산을 막습니다.
                $(window).off('scroll', onScroll);
                return;
            }
            const newPostHtml = createPostHtml(nextFeed);
            $feedContainer.append(newPostHtml);
        }).fail(error => {
            console.error("다음 피드 로딩 실패:", error);
            hasNext = false;
        }).always(() => {
            isLoading = false;
            $loadingIndicator.hide();
        });
    }

    function createPostHtml(feed) {
        // 이 함수는 작성자님의 기존 코드를 그대로 사용하며, 변경할 필요가 없습니다.
        const profileImageUrl = (feed.writerInfo && feed.writerInfo.prflImg) ? feed.writerInfo.prflImg : '/images/default-profile.png';
        const writerNickname = (feed.writerInfo && feed.writerInfo.userNcnm) ? feed.writerInfo.userNcnm : '알 수 없는 사용자';
        let imageHtml = '';
        if (feed.imageList && feed.imageList.length > 0) {
            feed.imageList.forEach(image => {
                imageHtml += `<img src="${image.imgFilePathNm}" alt="피드 이미지" class="main-feed-image">`;
            });
        }
        const feedContent = (feed.feedCn && feed.feedCn.trim() !== '') ? `<div class="feed-content-body"><p>${feed.feedCn}</p></div>` : '';

        // [변경] 작성자님의 코드스타일에 맞게 footer 내부 로직 간소화 및 통일
        return `
            <article class="feed-post" data-crt-dt="${feed.crtDt}" data-wrtr-user-no="${feed.wrtrUserNo}">
                <header class="feed-post-header">
                    <div class="user-profile">
                        <a href="#"><img src="${profileImageUrl}" alt="프로필 사진" class="profile-image"></a>
                        <div class="user-info"><span class="username">${writerNickname}</span></div>
                    </div>
                    <div class="post-actions">
                        <button class="follow-btn">+ 팔로우</button>
                        <button class="more-options-btn" aria-label="더보기">...</button>
                    </div>
                </header>
                <div class="feed-post-content">${imageHtml}</div>
                <footer class="feed-post-footer">
                    <!-- footer 내용은 createPostHtml 함수에서 생성되므로, 이 부분은 예시입니다. -->
                    <p class="like-count">좋아요 ${feed.likeCount}개</p>
                    ${feedContent}
                    <p class="post-timestamp">${feed.crtDt}</p>
                </footer>
            </article>
        `;
    }

    // --- 이벤트 핸들러 (이 부분이 핵심 변경 사항) ---
    
    // [변경] 기존의 wheel 이벤트 핸들러 대신, onScroll 함수를 정의합니다.
    const onScroll = () => {
        // (창의 맨 위부터 스크롤된 길이 + 현재 보이는 창의 높이) >= (전체 문서의 높이 - 버퍼)
        // 스크롤이 문서의 거의 끝에 도달했을 때
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100) { // 100px의 여유 버퍼
            loadNextFeed();
        }
    };

    // [변경] wheel 이벤트 대신 scroll 이벤트를 window에 등록합니다.
    $(window).on('scroll', onScroll);

});
/* ]]> */
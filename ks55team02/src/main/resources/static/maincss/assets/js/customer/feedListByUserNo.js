$(() => {
    // 1. 초기 설정: 페이지의 HTML로부터 필요한 정보를 가져옵니다.
    const container = $('.my-feed-container');
    const pageOwnerUserNo = container.data('page-owner-user-no');
    const loginUserNo = container.data('login-user-no');

    // 2. 무한 스크롤 상태 관리 변수를 선언합니다.
    let currentPage = 1;
    let isLoading = false;
    let hasNext = true;
    
    // 3. 현재 페이지가 '마이피드'인지 '타인피드'인지 판단하고, 그에 맞는 API URL을 설정합니다.
    const isMyFeed = pageOwnerUserNo.toString() === loginUserNo.toString();
    const apiUrl = isMyFeed 
                   ? '/customer/feed/my-feed' 
                   : `/customer/feed/user-feed/${pageOwnerUserNo}`;

    /**
     * 서버에서 받은 피드 데이터 1개를 HTML 구조로 변환하는 함수입니다.
     */
    const createFeedItemHtml = (feed) => {
        const defaultImageUrl = '/uploads/feeds/default_feed_image.jpg';
        const imageUrl = feed.representativeImage ? feed.representativeImage.imgFilePathNm : defaultImageUrl;
        const imageAlt = feed.representativeImage ? feed.representativeImage.imgAltTxtCn : '기본 이미지';
        
        // [핵심 로직] isMyFeed 값에 따라 상세 페이지 링크에 다른 context 파라미터를 추가합니다.
        let detailLink;
        if (isMyFeed) {
            // '내 피드' 목록에서는 'context=my' 파라미터를 추가합니다.
            detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=my`;
        } else {
            // '다른 사람의 피드' 목록에서는 'context=user'와 'userNo' 파라미터를 추가합니다.
            detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=user&userNo=${pageOwnerUserNo}`;
        }
        
        return `
            <article class="feed-item">
                <a href="${detailLink}">
                    <img src="${imageUrl}" alt="${imageAlt}">
                    <div class="item-overlay">
                        <span class="likes">♥ ${feed.likeCount}</span>
                    </div>
                </a>
            </article>
        `;
    };
    
    /**
     * 받아온 피드 목록(HTML)을 페이지의 그리드 컨테이너에 추가하는 함수입니다.
     */
    const renderFeeds = (feeds) => {
        const gridContainer = $('#feed-grid-container');
        if (currentPage === 1 && feeds.length === 0) {
            let message = isMyFeed ? '작성한 피드가 없습니다.' : '이 사용자가 작성한 피드가 없습니다.';
            gridContainer.html(`<div class="no-feeds" style="text-align:center; padding: 20px;"><p>${message}</p></div>`);
            hasNext = false;
            return;
        }
        feeds.forEach(feed => {
            gridContainer.append(createFeedItemHtml(feed));
        });
    };

    /**
     * 다음 페이지의 피드 데이터를 서버에 비동기적으로 요청하는 함수입니다.
     */
    const loadNextPage = () => {
        if (isLoading || !hasNext) return;
        isLoading = true;
        $('#loading-indicator').show();

        $.ajax({
            url: apiUrl,
            type: 'GET',
            data: { page: currentPage },
            dataType: 'json'
        })
        .done(response => {
            renderFeeds(response.feedList); 
            hasNext = response.hasNext;     
            currentPage++;                  
        })
        .fail((jqXHR) => {
            console.error('피드 로딩 중 오류 발생:', jqXHR.statusText);
            if (jqXHR.status === 401 && isMyFeed) {
                alert('세션이 만료되었거나 로그인이 필요합니다. 로그인 페이지로 이동합니다.');
                window.location.href = '/customer/login/login?redirectUrl=' + window.location.pathname;
            }
        })
        .always(() => {
            isLoading = false;
            $('#loading-indicator').hide();
        });
    };

    /**
     * 스크롤 이벤트를 감지하여 다음 페이지 로드를 트리거하는 함수입니다.
     */
    const onScroll = () => {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 200) {
            if (hasNext) loadNextPage();
        }
    };

    // 스크롤 이벤트 리스너를 등록하고, 첫 페이지 데이터를 로드합니다.
    $(window).on('scroll', onScroll);
    loadNextPage();
});
$(() => {
    // 1. HTML로부터 사용자 정보 가져오기
	const container = $('.my-feed-container');
	const pageOwnerUserNo = container.data('page-owner-user-no');
	const loginUserNo = container.data('login-user-no');
    
    // 2. API 요청을 위한 변수 설정
    let currentPage = 1;
    let isLoading = false;
    let hasNext = true;
    
    // 3. 페이지 주인과 로그인 유저가 같은지 판단하여 API 엔드포인트를 동적으로 결정
    const isMyFeed = pageOwnerUserNo.toString() === loginUserNo.toString();
    const apiUrl = isMyFeed 
                   ? '/customer/feed/my-feed' 
                   : `/customer/feed/user-feed/${pageOwnerUserNo}`;

    /**
     * 피드 아이템 1개에 대한 HTML을 생성하는 함수 (제공해주신 코드 기반)
     */
    const createFeedItemHtml = (feed) => {
        const defaultImageUrl = '/uploads/feeds/default_feed_image.jpg'; // 기본 이미지 경로
        const imageUrl = feed.representativeImage ? feed.representativeImage.imgFilePathNm : defaultImageUrl;
        const imageAlt = feed.representativeImage ? feed.representativeImage.imgAltTxtCn : '기본 이미지';
        
        return `
            <article class="feed-item">
                <a href="/customer/feed/feedDetail/${feed.feedSn}">
                    <img src="${imageUrl}" alt="${imageAlt}">
                    <div class="item-overlay">
                        <span class="likes">♥ ${feed.likeCount}</span>
                    </div>
                </a>
            </article>
        `;
    };

    /**
     * 서버에서 받아온 피드 목록을 화면에 렌더링하는 함수
     */
    const renderFeeds = (feeds) => {
        const container = $('#feed-grid-container');
        
        if (currentPage === 1 && feeds.length === 0) {
            let message = isMyFeed ? '작성한 피드가 없습니다.' : '이 사용자가 작성한 피드가 없습니다.';
            container.html(`<div class="no-feeds" style="text-align:center; padding: 20px;"><p>${message}</p></div>`);
            hasNext = false; // 더 이상 로드할 필요 없음
            return;
        }

        feeds.forEach(feed => {
            container.append(createFeedItemHtml(feed));
        });
    };

    /**
     * 다음 페이지의 피드 데이터를 비동기적으로 로드하는 함수
     */
    const loadNextPage = () => {
        if (isLoading || !hasNext) {
            return;
        }
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
                // '마이피드' 페이지에서 401 오류 발생 시에만 로그인 페이지로 리다이렉트
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
     * 스크롤 이벤트를 감지하여 다음 페이지를 로드하는 함수
     */
    const onScroll = () => {
        // 페이지 하단에서 200px 위에 도달했을 때 다음 페이지 로드
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 200) {
            if (hasNext) {
                loadNextPage();
            }
        }
    };

    // 스크롤 이벤트 리스너 등록
    $(window).on('scroll', onScroll);
    
    // 페이지 첫 로드 시, 첫 페이지 데이터 가져오기
    loadNextPage();
});
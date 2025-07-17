$(() => {
    const container = $('.my-feed-container');
    const pageOwnerUserNo = container.data('page-owner-user-no');
    const loginUserNo = container.data('login-user-no');

    let currentPage = 1;
    let isLoading = false;
    let hasNext = true;
    
    const isMyFeed = pageOwnerUserNo === loginUserNo;

    // [핵심 수정] 변경된 RestController의 경로에 맞춰 API URL을 수정합니다.
    const apiUrl = isMyFeed 
                   ? '/customer/api/feeds/my-feed' 
                   : `/customer/api/feeds/user-feed/${pageOwnerUserNo}`;

    const createFeedItemHtml = (feed) => {
        const defaultImageUrl = '/uploads/feeds/default_feed_image.jpg';
        const imageUrl = feed.representativeImage ? feed.representativeImage.imgFilePathNm : defaultImageUrl;
        const imageAlt = feed.representativeImage ? feed.representativeImage.imgAltTxtCn : '기본 이미지';
        
        let detailLink;
        if (isMyFeed) {
            detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=my`;
        } else {
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

    const onScroll = () => {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 200) {
            if (hasNext) loadNextPage();
        }
    };

    $(window).on('scroll', onScroll);
    loadNextPage();
});
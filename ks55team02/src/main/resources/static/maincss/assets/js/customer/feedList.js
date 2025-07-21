$(() => {
    let currentPage = 1;
    let isLoading = false;
    let hasNext = true; 

    const createFeedItemHtml = (feed) => {
        const defaultImageUrl = '/images/default-feed.png'; 
        const imageUrl = feed.representativeImage?.imgFilePathNm || defaultImageUrl;
        const imageAlt = feed.representativeImage?.imgAltTxtCn || '피드 대표 이미지';
        const writerNickname = feed.writerInfo?.userNcnm || '알 수 없는 사용자';
		const detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=all`;
		
        return `
            <article class="feed-item">
                <a href="${detailLink}">
                    <img src="${imageUrl}" alt="${imageAlt}">
                    <div class="item-overlay">
                        <span class="likes">♥ ${feed.likeCount}</span>
                    </div>
                </a>
                <div class="feed-writer-info">
                    <span>${writerNickname}</span>
                </div>
            </article>
        `;
    };

    const renderFeeds = (feeds) => {
        const container = $('#feed-list-grid-container');
        if (currentPage === 1 && feeds.length === 0) {
            container.html('<div class="no-feeds" style="text-align:center; padding: 20px;"><p>등록된 피드가 없습니다.</p></div>');
            return;
        }
        feeds.forEach(feed => {
            container.append(createFeedItemHtml(feed));
        });
    };

    const loadNextPage = () => {
        if (isLoading || !hasNext) {
            return;
        }
        isLoading = true;
        $('#loading-indicator').show();

        $.ajax({
            url: '/customer/api/feeds/list', 
            type: 'GET',
            data: { page: currentPage },
            dataType: 'json'
        })
        .done(response => {
			if (currentPage === 1) {
			    const totalCount = response.totalCount || 0;
			    $('.content-count').text(`총 ${totalCount.toLocaleString()}개`);
			}
            renderFeeds(response.feedList); 
            hasNext = response.hasNext;     
            currentPage++;                  
        })
        .fail((jqXHR) => {
            console.error('피드 로딩 중 오류 발생:', jqXHR.statusText);
        })
        .always(() => {
            isLoading = false;
            $('#loading-indicator').hide();
        });
    };

    const onScroll = () => {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 200) {
            if (hasNext) {
                loadNextPage();
            }
        }
    };

    $(window).on('scroll', onScroll);
    loadNextPage();
});
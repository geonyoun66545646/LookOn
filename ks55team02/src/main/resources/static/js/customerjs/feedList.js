// /js/customerjs/feedList.js (최종 수정본)

$(document).ready(function() {
    const $feedContainer = $('.feed-container');
    let currentPage = parseInt($feedContainer.data('currentPage'), 10);
    let hasNext = $feedContainer.data('hasNext');
    let activeTab = $feedContainer.data('activeTab');
    let isLoading = false;

    // 로딩 인디케이터 DOM 요소를 변수로 캐싱합니다.
    const $loadingIndicator = $('#loading-indicator');

    function loadFeeds(page, replace = false) {
        if (isLoading) return;
        isLoading = true;
        
        // 로딩 시작 시, 인디케이터를 보여줍니다.
        $loadingIndicator.show();

        const sortOrder = $('#sort-dropdown').val();
        const url = activeTab === 'following' ? '/customer/api/feeds/following' : '/customer/api/feeds/list';
        
        if (activeTab === 'following' && !window.loginUser) {
            alert("로그인이 필요한 기능입니다.");
            $('#signin-modal').modal('show');
            isLoading = false;
            $loadingIndicator.hide();
            return;
        }

        $.ajax({
            url: url,
            type: 'GET',
            data: {
                page: page,
                sort: sortOrder
            },
            dataType: 'json'
        })
        .done(function(response) {
            if (replace) {
                $('#feed-list-grid-container').empty();
            }
            
            if (response.feedList && response.feedList.length > 0) {
                renderFeeds(response.feedList);
                hasNext = response.hasNext;
            } else {
                hasNext = false;
                if (replace) {
                    const message = activeTab === 'following' ? '<p>팔로우하는 사용자의 피드가 없습니다.</p>' : '<p>등록된 피드가 없습니다.</p>';
                    $('#feed-list-grid-container').html(`<div class="no-feeds">${message}</div>`);
                }
            }
            currentPage = page;
        })
        .fail(function(xhr) {
            console.error("피드를 불러오는데 실패했습니다.", xhr.responseText);
            hasNext = false; // 실패 시 더 이상 시도하지 않도록 설정
            if (replace) {
                $('#feed-list-grid-container').html('<div class="no-feeds"><p>오류가 발생했습니다. 잠시 후 다시 시도해주세요.</p></div>');
            }
        })
        .always(function() {
            isLoading = false;
            // 로딩이 끝나면, 다음 페이지가 있을 때만 인디케이터를 남겨두고 없으면 숨깁니다.
            if (!hasNext) {
                $loadingIndicator.hide();
            }
        });
    }

    function renderFeeds(feedList) {
        let feedItemsHtml = '';
        feedList.forEach(feed => {
            const defaultFeedImg = '/images/default-feed.png';
            const defaultProfileImg = '/uploads/profiles/defaultprofile.jpg';
            const representativeImage = feed.representativeImage?.imgFilePathNm || defaultFeedImg;
            const imageAltText = feed.representativeImage?.imgAltTxtCn || '피드 대표 이미지';
            const profileImage = feed.writerInfo?.prflImg || defaultProfileImg;
            const userNickname = feed.writerInfo?.userNcnm || '알 수 없는 사용자';
            const likeCount = feed.likeCount || 0;

            feedItemsHtml += `
                <article class="feed-item">
                    <a href="/customer/feed/feedDetail/${feed.feedSn}?context=all" class="feed-image-link">
                        <img src="${representativeImage}" alt="${imageAltText}">
                        <div class="item-overlay">
                            <span class="likes">♥ ${likeCount}</span>
                        </div>
                    </a>
                    <div class="feed-writer-info">
                        <a href="/customer/feed/feedListByUserNo/${feed.wrtrUserNo}" class="writer-profile-link">
                            <img src="${profileImage}" alt="${userNickname} 프로필" class="writer-profile-image">
                            <span>${userNickname}</span>
                        </a>
                    </div>
                </article>`;
        });
        $('#feed-list-grid-container').append(feedItemsHtml);
    }
    
    $('#discover-tab, #following-tab').on('click', function(e) {
        e.preventDefault();
        
        const newActiveTab = $(this).is('#following-tab') ? 'following' : 'discover';
        if (newActiveTab === activeTab) return;
        activeTab = newActiveTab; // [오타 수정] newActiveTba -> newActiveTab
        
        $('.feed-tabs li').removeClass('active');
        $(this).parent('li').addClass('active');
        
        // 탭/정렬 변경 시, 다음 페이지가 있을 것이라 가정하고 인디케이터를 다시 보여줍니다.
        $loadingIndicator.show(); 
        loadFeeds(1, true); 
    });

    $('#sort-dropdown').on('change', function() {
        // 탭/정렬 변경 시, 다음 페이지가 있을 것이라 가정하고 인디케이터를 다시 보여줍니다.
        $loadingIndicator.show();
        loadFeeds(1, true);
    });

    const observerTarget = document.getElementById('loading-indicator');
    if (observerTarget) {
        const observer = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && !isLoading && hasNext) {
                loadFeeds(currentPage + 1, false);
            }
        }, { threshold: 0.1 });
        
        observer.observe(observerTarget);
    }
});
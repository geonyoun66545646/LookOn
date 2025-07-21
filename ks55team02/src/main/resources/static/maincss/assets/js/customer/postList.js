/**
 * postList.js v2.3 (대표 이미지 DTO 필드명 변경 반영)
 * - 썸네일 이미지 경로를 post.representativeImage.imgFilePathNm에서 참조하도록 수정
 */
$(() => {

    const state = {
        currentPage: 1,
        isLoading: false,
        hasNext: true,
        bbsClsfCd: $('.container').data('initial-bbsclsfcd') || ''
    };

    const $container = $('#post-list-container');
    const $tabsParentContainer = $('.board-tabs-container');
    const $navTabsScroller = $tabsParentContainer.find('.nav-tabs');
    const $loadingIndicator = $('#loading-indicator');

    const slider = $navTabsScroller[0];
    if (slider) {
        let isDown = false, startX, scrollLeft;
        const dragStart = (e) => {
            if (e.target.tagName === 'A' || $(e.target).closest('a').length) {
                e.preventDefault();
            }
            isDown = true;
            $tabsParentContainer.addClass('active-drag');
            startX = e.pageX - slider.offsetLeft;
            scrollLeft = slider.scrollLeft;
        };
        const dragEnd = () => { isDown = false; $tabsParentContainer.removeClass('active-drag'); };
        const dragMove = (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - slider.offsetLeft;
            const walk = (x - startX) * 2;
            slider.scrollLeft = scrollLeft - walk;
        };
        $tabsParentContainer.on('mousedown', dragStart);
        $tabsParentContainer.on('mouseleave', dragEnd);
        $tabsParentContainer.on('mouseup', dragEnd);
        $tabsParentContainer.on('mousemove', dragMove);
    }

    $navTabsScroller.on('click', '.nav-link', function(e) {
        e.preventDefault();
        const selectedBbsClsfCd = $(this).data('bbsclsfcd');
        if ($(this).hasClass('active')) return;

        $navTabsScroller.find('.nav-link').removeClass('active');
        $(this).addClass('active');

        state.bbsClsfCd = selectedBbsClsfCd;
        state.currentPage = 1;
        state.hasNext = true;
        $container.empty();
        loadNextPage();
    });

    $(window).on('scroll', () => {
        if (state.isLoading || !state.hasNext) return;
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 300) {
            loadNextPage();
        }
    });

    function loadNextPage() {
        state.isLoading = true;
        $loadingIndicator.show();

        $.ajax({
            url: '/customer/api/post/list',
            type: 'GET',
            data: {
                page: state.currentPage,
                size: 15,
                bbsClsfCd: state.bbsClsfCd
            },
            dataType: 'json'
        })
        .done(response => {
            if (response && response.postList && response.postList.length > 0) {
                renderPostList(response.postList);
                state.hasNext = response.hasNext;
                state.currentPage++;
            } else {
                state.hasNext = false;
                if (state.currentPage === 1) {
                    $container.html('<div class="no-posts-message">게시글이 없습니다.</div>');
                }
            }
        })
        .fail(err => {
            console.error("게시글 로딩 중 오류:", err);
            state.hasNext = false;
        })
        .always(() => {
            state.isLoading = false;
            $loadingIndicator.hide();
        });
    }

	function renderPostList(postList) {
	    function formatRelativeTime(dateString) {
	        if (!dateString) return '';
	        const now = new Date();
	        const postDate = new Date(dateString);
	        const diffInSeconds = Math.floor((now - postDate) / 1000);
	        const MINUTE = 60, HOUR = 3600, DAY = 86400, WEEK = 604800;
	        if (diffInSeconds < MINUTE) return '방금 전';
	        if (diffInSeconds < HOUR) return `${Math.floor(diffInSeconds / MINUTE)}분 전`;
	        if (diffInSeconds < DAY) return `${Math.floor(diffInSeconds / HOUR)}시간 전`;
	        if (diffInSeconds < WEEK) return `${Math.floor(diffInSeconds / DAY)}일 전`;
	        return dateString.split('T')[0].replace(/-/g, '.');
	    }

	    const eyeIconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-eye-fill" viewBox="0 0 16 16"><path d="M10.5 8a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0z"/><path d="M0 8s3-5.5 8-5.5S16 8 16 8s-3 5.5-8 5.5S0 8 0 8zm8 3.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7z"/></svg>`;
	    const commentIconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-chat-square-fill" viewBox="0 0 16 16"><path d="M2 0a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2.5a1 1 0 0 1 .8.4l1.9 2.533a1 1 0 0 0 1.6 0l1.9-2.533a1 1 0 0 1 .8-.4H14a2 2 0 0 0 2-2V2a2 2 0 0 0-2-2H2z"/></svg>`;

	    postList.forEach(post => {
	        // [핵심 수정] 썸네일 이미지 경로 생성 로직
	        let thumbnailHtml = '';
	        if (post.representativeImage && post.representativeImage.imgFilePathNm) {
	            const thumbnailUrl = post.representativeImage.imgFilePathNm;
	            thumbnailHtml = `
	                <div class="post-item-thumbnail">
	                    <img src="${thumbnailUrl}" alt="${post.pstTtl || '게시글 썸네일'}">
	                </div>`;
	        }
	        
	        const formattedDate = formatRelativeTime(post.crtDt);
	        const detailLink = `/customer/post/postView?pstSn=${post.pstSn}`;
	        
	        const postItemHtml = `
	            <div class="post-item">
	                <a href="${detailLink}">
	                    <div class="post-item-content">
	                        <h3 class="title">${post.pstTtl || '제목 없음'}</h3>
	                        <div class="author-info">
	                            <span class="nickname">${post.writerInfo?.userNcnm || '작성자 없음'}</span>
	                            <span class="separator">·</span>
	                            <span class="date">${formattedDate}</span>
	                        </div>
	                        <div class="post-stats">
	                            <span class="views">${eyeIconSvg} <span>${post.viewCnt || 0}</span></span>
	                            <span class="comments">${commentIconSvg} <span>${post.cmntCnt || 0}</span></span>
	                        </div>
	                    </div>
	                    ${thumbnailHtml}
	                </a>
	            </div>`;
	        $container.append(postItemHtml);
	    });
	}

    loadNextPage();
});
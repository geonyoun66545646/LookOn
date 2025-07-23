/**
 * postList.js v2.4
 * - [추가] 팔로잉 탭에 대한 로그인 체크 로직 추가
 * - [수정] 대표 이미지 DTO 필드명 변경 반영 (기존)
 */
$(() => {
    // --- 1. 상태 관리 변수 ---
    const state = {
        currentPage: 1,
        isLoading: false,
        hasNext: true,
        bbsClsfCd: $('.container').data('initial-bbsclsfcd') || ''
    };
    
    // --- 2. 주요 DOM 요소 캐싱 ---
    const $container = $('#post-list-container');
    const $tabsParentContainer = $('.board-tabs-container');
    const $navTabsScroller = $tabsParentContainer.find('.nav-tabs');
    const $loadingIndicator = $('#loading-indicator');
    
    // --- 3. 전역 변수 (로그인 정보) ---
    // HTML의 <script> 블록에서 설정한 window.loginUser 값을 가져옵니다.
    const loginUser = window.loginUser || null;

    // --- 4. UI/UX 관련 로직 (카테고리 탭 드래그 스크롤) ---
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
    
    // --- 5. 이벤트 핸들러 ---

    // 하위 카테고리 탭(전체, 자유게시판 등) 클릭 이벤트
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
    
    // [핵심 추가] 상단 메인 탭의 '팔로잉' 링크 클릭 이벤트
    $('#following-tab-link').on('click', function(e) {
        e.preventDefault(); // a 태그의 기본 동작(링크 이동)을 막습니다.
        
        // 1. 비로그인 상태인지 확인합니다.
        if (!loginUser) {
            // 비로그인 상태이면, 로그인 모달을 띄웁니다.
            $('#signin-modal').modal('show');
            return; // 여기서 함수 실행을 중단합니다.
        }
        
        // 2. 로그인 상태라면, HTML의 data-href 속성에 저장해둔 피드 페이지 URL로 이동시킵니다.
        const targetUrl = $(this).data('href');
        if (targetUrl) {
            window.location.href = targetUrl;
        }
    });

    // 무한 스크롤 이벤트
    $(window).on('scroll', () => {
        if (state.isLoading || !state.hasNext) return;
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 300) {
            loadNextPage();
        }
    });
    
    // --- 6. 데이터 처리 및 렌더링 함수 ---

    // 다음 페이지 데이터를 불러오는 함수
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
				    const noPostHtml = `
				        <div class="post-item">
				            <div class="post-item-content" style="text-align: center; width: 100%;">
				                <h3 class="title" style="margin: 3rem 0;">게시글이 없습니다.</h3>
				            </div>
				        </div>
				    `;
				    $container.html(noPostHtml);
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

    // 게시글 목록 HTML을 생성하고 렌더링하는 함수
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
	        let thumbnailHtml = '';
	        if (post.representativeImage && post.representativeImage.imgFilePathNm) {
	            const thumbnailUrl = post.representativeImage.imgFilePathNm;
	            thumbnailHtml = `
	                <div class="post-item-thumbnail">
	                    <img src="${thumbnailUrl}" alt="${post.pstTtl || '게시글 썸네일'}">
	                </div>`;
	        }
	        
	        const formattedDate = formatRelativeTime(post.crtDt);
	        const detailLink = `/customer/post/postDetail?pstSn=${post.pstSn}`;
	        
			const isNoticeClass = post.bbsClsfCd === 'notice' ? ' is-notice' : '';

			const postItemHtml = `
			    <div class="post-item${isNoticeClass}">
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
    
    // --- 7. 초기 실행 ---
    loadNextPage();
});
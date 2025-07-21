$(() => {
    // --- 1. 전역 변수 및 초기 설정 ---
    const container = $('#feed-container');
    const context = container.data('context');
    const loginUserNo = container.data('login-user-no');
    const INITIAL_COMMENT_COUNT = 5; // 처음에 보여줄 댓글 개수
    let isLoading = false;
    let hasNext = true;

    // --- 2. 헬퍼 함수 (HTML 생성 및 UI 업데이트) ---

    /**
     * 댓글 객체를 받아 <li> HTML 문자열로 변환합니다.
     * 수정 모드를 위한 구조가 포함되어 있습니다.
     */
    const createCommentHtml = (comment, isHiddenClass = '') => {
        const writerProfileImg = comment.writerInfo?.prflImg || '/uploads/profiles/defaultprofile.jpg';
        const writerNickname = comment.writerInfo?.userNcnm || '작성자';
        const writerLink = `/customer/feed/feedListByUserNo/${comment.writerInfo?.userNo}`;
        const commentTimestamp = (comment.mdfcnDt || comment.crtDt).substring(0, 16);
        const editedMark = comment.mdfcnDt ? ' (수정됨)' : '';
        
        return `
            <li class="comment-item ${isHiddenClass}" data-comment-sn="${comment.feedCmntSn}" data-writer-user-no="${comment.wrtrUserNo}">
                <div class="comment-writer-profile"><a href="${writerLink}"><img src="${writerProfileImg}" alt="프로필 사진"></a></div>
                <div class="comment-content">
                    <div class="comment-view-mode">
                        <p><a href="${writerLink}" class="comment-writer-username">${writerNickname}</a> <span class="comment-text">${comment.cmntCn}</span></p>
                        <span class="comment-timestamp">${commentTimestamp}${editedMark}</span>
                    </div>
                    <div class="comment-edit-mode" style="display: none;">
                        <textarea class="comment-edit-input">${comment.cmntCn}</textarea>
                        <div class="edit-actions"><button class="cancel-edit-btn">취소</button><button class="save-edit-btn">저장</button></div>
                    </div>
                </div>
                <div class="comment-actions"></div>
            </li>`;
    };

    /**
     * 피드 객체를 받아 전체 피드 포스트 HTML 구조를 생성합니다. (무한 스크롤용)
     */
    const createFeedDetailHtml = (feed) => {
        const writerNickname = feed.writerInfo?.userNcnm || '알 수 없는 사용자';
        const writerProfileImg = feed.writerInfo?.prflImg || '/uploads/profiles/defaultprofile.jpg';
        const writerProfileLink = `/customer/feed/feedListByUserNo/${feed.wrtrUserNo}`;
        const imageListHtml = feed.imageList?.map(image => `<img src="${image.imgFilePathNm}" alt="피드 이미지" class="main-feed-image">`).join('') || '';
        
        const feedContentHtml = feed.feedCn ? `<div class="feed-content-body"><a href="${writerProfileLink}"><img src="${writerProfileImg}" alt="${writerNickname} 프로필" class="writer-profile-img"></a><p><a href="${writerProfileLink}" class="comment-writer-username">${writerNickname}</a> <span class="feed-text">${feed.feedCn}</span></p></div>` : '';
        let hashtagsHtml = '';
        if (feed.tagList?.length > 0) {
            hashtagsHtml = `<div class="hashtags">${feed.tagList.map(tag => `<a href="#">#${tag.tagNm}</a>`).join(' ')}</div>`;
        }
        const feedContentWrapperHtml = `<div class="feed-content-wrapper">${feedContentHtml}${hashtagsHtml}</div>`;

        const comments = feed.commentList || [];
        const commentListHtml = comments.map((comment, index) => createCommentHtml(comment, index >= INITIAL_COMMENT_COUNT ? 'hidden-comment' : '')).join('');
        
        let showMoreBtnHtml = '';
        if (comments.length > INITIAL_COMMENT_COUNT) {
            showMoreBtnHtml = `<div class="show-more-wrapper"><button class="show-more-comments-btn">댓글 ${comments.length - INITIAL_COMMENT_COUNT}개 더 보기</button></div>`;
        }

        const postTimestamp = feed.crtDt ? feed.crtDt.substring(0, 16) : '';
        const isMyPost = loginUserNo && loginUserNo === feed.wrtrUserNo;
		
		// [수정] 기존 '수정/삭제' 버튼 대신 항상 '...' 버튼이 보이도록 변경
		const postActionsHtml = `<button class="options-menu-btn" aria-label="옵션 더 보기"><svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><circle cx="12" cy="12" r="1.5"></circle><circle cx="6" cy="12" r="1.5"></circle><circle cx="18" cy="12" r="1.5"></circle></svg></button>`;

		// [신규 추가] 팔로우 버튼이 들어갈 영역을 생성합니다.
        const followActionHtml = !isMyPost ? `<div class="follow-action-area"></div>` : '';
		
		return `<div class="feed-post-wrapper" data-crt-dt="${feed.crtDt}" data-wrtr-user-no="${feed.wrtrUserNo}" data-feed-sn="${feed.feedSn}"><section class="post-media-section">...</section><section class="post-info-section"><article class="feed-post"><header class="feed-post-header"><div class="user-profile">...<div class="user-info">...</div>${followActionHtml}</div><div class="post-actions">${postActionsHtml}</div></header>...</article></section></div>`;
    };

    /**
     * 댓글 권한을 확인하여 '수정/삭제' 아이콘을 추가하는 함수
     */
    const applyCommentActions = () => {
        $('.comment-item:not(.actions-checked)').each(function() {
            const commentItem = $(this);
            if (loginUserNo && loginUserNo === commentItem.data('writer-user-no')) {
                const actionsHtml = `<button class="edit-comment-btn" aria-label="댓글 수정"><svg viewBox="0 0 24 24" width="16" height="16"><path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.13,5.12L18.88,8.87M3,17.25V21H6.75L17.81,9.94L14.06,6.19L3,17.25Z"></path></svg></button><button class="delete-comment-btn" aria-label="댓글 삭제"><svg viewBox="0 0 24 24" width="16" height="16"><path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"></path></svg></button>`;
                commentItem.find('.comment-actions').html(actionsHtml);
            }
            commentItem.addClass('actions-checked');
        });
    };

    // --- 3. 무한 스크롤 로직 ---
    const loadNextFeed = () => {
        if (isLoading || !hasNext) return;
        const lastFeedWrapper = $('.feed-post-wrapper').last();
        const currentFeedCrtDt = lastFeedWrapper.data('crt-dt');
        if (!currentFeedCrtDt) return;
        isLoading = true;
        $('#loading-indicator').show();
        $.ajax({ url: '/customer/api/feeds/next', type: 'GET', data: { currentFeedCrtDt, limit: 3, context: container.data('user-no') }, dataType: 'json' })
        .done(nextFeedList => {
            if (nextFeedList?.length > 0) {
                nextFeedList.forEach(nextFeed => container.append(createFeedDetailHtml(nextFeed)));
                applyCommentActions();
				// ▼▼▼ [신규 추가] 새로 로드된 피드에 팔로우 버튼을 생성하도록 함수 호출 ▼▼▼
                initializeAllFollowButtons(); 
                // ▲▲▲ [신규 추가] ▲▲▲
            } else {
                hasNext = false;
                $('#loading-indicator').html('<p>마지막 피드입니다.</p>').show();
            }
        })
        .fail(err => { console.error("다음 피드 로딩 실패:", err); hasNext = false; })
        .always(() => { isLoading = false; if (hasNext) $('#loading-indicator').hide(); });
    };
    $(window).on('scroll', () => {
        if ($(window).scrollTop() + $(window).height() >= $('body').get(0).scrollHeight - 200) {
            loadNextFeed();
        }
    });

    // --- 4. 모든 이벤트 핸들러 (이벤트 위임 방식) ---
    container
        .on('click', '.interaction-buttons button[aria-label="좋아요"]', function(e) {
            e.preventDefault();
            const likeButton = $(this);
            if (likeButton.hasClass('liked')) return;
            const wrapper = likeButton.closest('.feed-post-wrapper');
            const feedSn = wrapper.data('feed-sn');
            if (!feedSn) return;
            $.ajax({ url: `/customer/api/feeds/${feedSn}/like`, type: 'POST' })
            .done(response => {
                likeButton.addClass('liked');
                wrapper.find('.like-count').text(`좋아요 ${response.likeCount}개`);
            })
            .fail(jqXHR => {
                if (jqXHR.status === 401) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/customer/login/login?redirectUrl=' + window.location.pathname;
                } else { alert('요청에 실패했습니다.'); }
            });
        })
        .on('keypress', '.comment-input', function(e) {
            if (e.which === 13) {
                e.preventDefault();
                const inputField = $(this);
                const commentText = inputField.val().trim();
                const wrapper = inputField.closest('.feed-post-wrapper');
                const feedSn = wrapper.data('feed-sn');
                if (commentText === '' || !feedSn) return;
                $.ajax({ url: `/customer/api/feeds/${feedSn}/comments`, type: 'POST', data: { commentText } })
                .done(newComment => {
                    const newCommentHtml = createCommentHtml(newComment);
                    wrapper.find('.comment-list').append(newCommentHtml);
                    inputField.val('');
                    applyCommentActions();
                })
                .fail(jqXHR => {
                    if (jqXHR.status === 401) {
                        alert('로그인이 필요합니다.');
                        window.location.href = '/customer/login/login?redirectUrl=' + window.location.pathname;
                    } else { alert('댓글 작성에 실패했습니다.'); }
                });
            }
        })
        .on('click', '.delete-comment-btn', function(e) {
            e.preventDefault();
            if (!confirm('정말 이 댓글을 삭제하시겠습니까?')) return;
            const commentItem = $(this).closest('.comment-item');
            const commentSn = commentItem.data('comment-sn');
            if (!commentSn) return;
            $.ajax({ url: `/customer/api/feeds/comments/${commentSn}`, type: 'DELETE' })
            .done(() => {
                commentItem.fadeOut(300, function() { $(this).remove(); });
            })
            .fail(jqXHR => {
                if (jqXHR.status === 401 || jqXHR.status === 403) {
                    alert('삭제할 권한이 없습니다.');
                } else { alert('댓글 삭제에 실패했습니다.'); }
            });
        })
        .on('click', '.show-more-comments-btn', function(e) {
            e.preventDefault();
            const button = $(this);
            button.closest('.post-scrollable-content').find('.hidden-comment').removeClass('hidden-comment');
            button.parent().remove();
        })
        .on('click', '.edit-comment-btn', function(e) {
            e.preventDefault();
            const commentItem = $(this).closest('.comment-item');
            commentItem.find('.comment-view-mode').hide();
            commentItem.find('.comment-edit-mode').show().find('.comment-edit-input').focus();
        })
        .on('click', '.cancel-edit-btn', function(e) {
            e.preventDefault();
            const commentItem = $(this).closest('.comment-item');
            const originalText = commentItem.find('.comment-text').text();
            commentItem.find('.comment-edit-input').val(originalText);
            commentItem.find('.comment-view-mode').show();
            commentItem.find('.comment-edit-mode').hide();
        })
        .on('click', '.save-edit-btn', function(e) {
            e.preventDefault();
            const commentItem = $(this).closest('.comment-item');
            const commentSn = commentItem.data('comment-sn');
            const editedText = commentItem.find('.comment-edit-input').val().trim();
            if (editedText === '' || !commentSn) return;
            $.ajax({ url: `/customer/api/feeds/comments/${commentSn}`, type: 'PATCH', data: { commentText: editedText } })
            .done(updatedComment => {
                commentItem.find('.comment-text').text(updatedComment.cmntCn);
                const updatedTimestamp = (updatedComment.mdfcnDt || updatedComment.crtDt).substring(0, 16);
                commentItem.find('.comment-timestamp').text(updatedTimestamp + " (수정됨)");
                commentItem.find('.comment-view-mode').show();
                commentItem.find('.comment-edit-mode').hide();
            })
            .fail(jqXHR => {
                if (jqXHR.status === 401 || jqXHR.status === 403) {
                    alert('수정할 권한이 없습니다.');
                } else { alert('댓글 수정에 실패했습니다.'); }
            });
        })
        // [핵심 수정] 피드 '수정' 버튼 클릭 이벤트
        .on('click', '.edit-post-btn', function(e) {
            e.preventDefault();
            const wrapper = $(this).closest('.feed-post-wrapper');
            const feedSn = wrapper.data('feed-sn');
            if (!feedSn) {
                alert('피드 정보를 찾을 수 없습니다.');
                return;
            }
            // 수정 페이지로 이동
            window.location.href = `/customer/feed/edit/${feedSn}`;
        })

        // [핵심 수정] 피드 '삭제' 버튼 클릭 이벤트
        .on('click', '.delete-post-btn', function(e) {
            e.preventDefault();
            const wrapper = $(this).closest('.feed-post-wrapper');
            const feedSn = wrapper.data('feed-sn');
            if (!feedSn) {
                alert('피드 정보를 찾을 수 없습니다.');
                return;
            }
            
            if (confirm('정말로 이 피드를 삭제하시겠습니까? 삭제된 피드는 복구할 수 없습니다.')) {
                $.ajax({
                    url: `/customer/api/feeds/${feedSn}`,
                    type: 'DELETE'
                })
                .done(() => {
                    alert('피드가 성공적으로 삭제되었습니다.');
                    // 피드 목록 또는 메인 페이지로 이동
                    window.location.href = '/customer/feed/feedList'; 
                })
                .fail(jqXHR => {
                    if (jqXHR.status === 401 || jqXHR.status === 403) {
                        alert('삭제할 권한이 없습니다.');
                    } else {
                        alert('피드 삭제에 실패했습니다. 잠시 후 다시 시도해주세요.');
                    }
                });
            }
        });

    // --- 5. 초기 실행 ---
    applyCommentActions();
	
	// ▼▼▼ [신규 추가] follow.js와 연동하기 위한 함수 및 호출 코드 ▼▼▼
   /**
    * 화면에 있는 모든 피드 게시물에 대해 팔로우 버튼을 초기화하는 함수
    * (이 함수는 follow.js에 있는 initializeFollowButton 함수를 호출합니다)
    */
   function initializeAllFollowButtons() {
       $('.feed-post-header:not(.follow-initialized)').each(function() {
           const $header = $(this);
           const $wrapper = $header.closest('.feed-post-wrapper');
           const writerUserNo = $wrapper.data('wrtr-user-no');

           if (typeof initializeFollowButton === 'function') {
               initializeFollowButton($header, writerUserNo, loginUserNo);
           }
           $header.addClass('follow-initialized');
       });
   }
   
   // 페이지가 처음 로딩될 때 팔로우 버튼을 초기화합니다.
   initializeAllFollowButtons();
   // ▲▲▲ [신규 추가] ▲▲▲
});
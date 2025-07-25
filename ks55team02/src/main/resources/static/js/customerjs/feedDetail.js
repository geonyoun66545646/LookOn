// /js/customerjs/feedDetail.js (진정한 최종 완성본 2차)

$(() => {
	// --- 1. 전역 변수 및 초기 설정 ---
	const container = $('#feed-container');
	const loginUserNo = container.data('login-user-no');
	const INITIAL_COMMENT_COUNT = 5;
	let isLoading = false;
	let hasNext = true;
    const $loadingIndicator = $('#loading-indicator');

	// --- 2. 헬퍼 함수 (팔로우 버튼 초기화) ---
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

	// --- 3. 헬퍼 함수 (HTML 생성 및 UI 업데이트) ---
	const createCommentHtml = (comment, isHiddenClass = '') => {
		const writerProfileImg = comment.writerInfo?.prflImg || '/uploads/profiles/defaultprofile.jpg';
		const writerNickname = comment.writerInfo?.userNcnm || '작성자';
		const writerLink = `/customer/feed/feedListByUserNo/${comment.writerInfo?.userNo}`;
		const commentTimestamp = (comment.mdfcnDt || comment.crtDt).substring(0, 16);
		const editedMark = comment.mdfcnDt ? ' (수정됨)' : '';
		return `<li class="comment-item ${isHiddenClass}" data-comment-sn="${comment.feedCmntSn}" data-writer-user-no="${comment.wrtrUserNo}"><div class="comment-writer-profile"><a href="${writerLink}"><img src="${writerProfileImg}" alt="프로필 사진"></a></div><div class="comment-content"><div class="comment-view-mode"><p><a href="${writerLink}" class="comment-writer-username">${writerNickname}</a> <span class="comment-text">${comment.cmntCn}</span></p><span class="comment-timestamp">${commentTimestamp}${editedMark}</span></div><div class="comment-edit-mode" style="display: none;"><textarea class="comment-edit-input">${comment.cmntCn}</textarea><div class="edit-actions"><button class="cancel-edit-btn">취소</button><button class="save-edit-btn">저장</button></div></div></div><div class="comment-actions"></div></li>`;
	};

	const createFeedDetailHtml = (feed) => {
		const writerNickname = feed.writerInfo?.userNcnm || '알 수 없는 사용자';
		const writerProfileImg = feed.writerInfo?.prflImg || '/uploads/profiles/defaultprofile.jpg';
		const writerProfileLink = `/customer/feed/feedListByUserNo/${feed.wrtrUserNo}`;
		const imageListHtml = feed.imageList?.map(image => `<img src="${image.imgFilePathNm}" alt="피드 이미지" class="main-feed-image">`).join('') || '';
		const feedContentHtml = feed.feedCn ? `<div class="feed-content-body"><p class="feed-text">${feed.feedCn}</p></div>` : '';
		const hashtagsHtml = (feed.tagList?.length > 0) ? `<div class="hashtags">${feed.tagList.map(tag => `<a href="#">#${tag.tagNm}</a>`).join(' ')}</div>` : '';
        const comments = feed.commentList || [];
		const commentListHtml = comments.map((comment, index) => createCommentHtml(comment, index >= INITIAL_COMMENT_COUNT ? 'hidden-comment' : '')).join('');
        const noCommentsHtml = (comments.length === 0) ? `<p class="no-comments-message">아직 댓글이 없습니다.</p>` : '';
		const showMoreBtnHtml = (comments.length > INITIAL_COMMENT_COUNT) ? `<div class="show-more-wrapper"><button class="show-more-comments-btn">댓글 ${comments.length - INITIAL_COMMENT_COUNT}개 더 보기</button></div>` : '';
        const postTimestamp = feed.crtDt ? feed.crtDt.substring(0, 16) : '';
		const followActionHtml = (loginUserNo && loginUserNo != feed.wrtrUserNo) ? `<div class="follow-action-area"></div>` : '';
		const postActionsHtml = `<button class="options-menu-btn" aria-label="옵션 더 보기"><svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><circle cx="12" cy="12" r="1.5"></circle><circle cx="6" cy="12" r="1.5"></circle><circle cx="18" cy="12" r="1.5"></circle></svg></button>`;
		
        return `<div class="feed-post-wrapper" data-crt-dt="${feed.crtDt}" data-wrtr-user-no="${feed.wrtrUserNo}" data-feed-sn="${feed.feedSn}">
                    <section class="post-media-section"><div class="feed-post-content">${imageListHtml}</div></section>
                    <section class="post-info-section"><article class="feed-post">
                            <header class="feed-post-header">
                                <div class="user-profile">
                                    <a href="${writerProfileLink}"><img src="${writerProfileImg}" alt="프로필 사진" class="profile-image"></a>
                                    <div class="user-info"><span class="username">${writerNickname}</span></div>
                                    ${followActionHtml}
                                </div>
                                <div class="post-actions">${postActionsHtml}</div>
                            </header>
                            <div class="post-scrollable-content">
                                <ul class="comment-list">${commentListHtml}</ul>
                                ${noCommentsHtml}
                                ${showMoreBtnHtml}
                            </div>
                            <footer class="feed-post-footer">
                                <div class="interaction-buttons">
                                    <button aria-label="좋아요"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"></path></svg></button>
                                    <button aria-label="댓글"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"></path></svg></button>
                                    <button aria-label="공유"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3s3-1.34 3-3-1.34-3-3-3z"></path></svg></button>
                                </div>
                                <p class="like-count">좋아요 ${feed.likeCount}개</p>
                                ${feedContentHtml}
                                ${hashtagsHtml}
                                <p class="post-timestamp">${postTimestamp}</p>
                                <div class="comment-input-area"><input type="text" placeholder="댓글 남기기..." class="comment-input"></div>
                            </footer>
                        </article></section>
                </div>`;
	};

	// [전체 코드 제공] 댓글 수정/삭제 버튼을 동적으로 추가하는 함수
	const applyCommentActions = () => {
		$('.comment-item:not(.actions-checked)').each(function() {
			const commentItem = $(this);
			if (loginUserNo && loginUserNo === String(commentItem.data('writer-user-no'))) {
				const actionsHtml = `<button class="edit-comment-btn" aria-label="댓글 수정"><svg viewBox="0 0 24 24" width="16" height="16"><path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.13,5.12L18.88,8.87M3,17.25V21H6.75L17.81,9.94L14.06,6.19L3,17.25Z"></path></svg></button><button class="delete-comment-btn" aria-label="댓글 삭제"><svg viewBox="0 0 24 24" width="16" height="16"><path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"></path></svg></button>`;
				commentItem.find('.comment-actions').html(actionsHtml);
			}
			commentItem.addClass('actions-checked');
		});
	};

	// --- 4. 데이터 로딩 함수 (다음 피드) ---
	const loadNextFeed = (observer) => {
		if (isLoading || !hasNext) return;
		isLoading = true;
		$loadingIndicator.html('<p>Loading...</p>');
		
		const lastFeedWrapper = $('.feed-post-wrapper').last();
		const currentFeedCrtDt = lastFeedWrapper.data('crt-dt');

		if (!currentFeedCrtDt) {
			isLoading = false;
			$loadingIndicator.empty();
			return;
		}

		$.ajax({
            url: '/customer/api/feeds/next',
            type: 'GET',
            data: { currentFeedCrtDt, limit: 3, context: container.data('context'), userNo: container.data('user-no') }
        })
		.done(nextFeedList => {
			if (nextFeedList && nextFeedList.length > 0) {
				nextFeedList.forEach(nextFeed => {
                    $loadingIndicator.before(createFeedDetailHtml(nextFeed));
                });
				applyCommentActions();
				initializeAllFollowButtons();
			} else {
				hasNext = false;
				$loadingIndicator.html('<p>마지막 피드입니다.</p>');
				if (observer) observer.disconnect();
			}
		}).fail(err => {
			console.error("다음 피드 로딩 실패:", err);
			hasNext = false;
			if (observer) observer.disconnect();
		}).always(() => {
			isLoading = false;
			if (hasNext) {
				$loadingIndicator.empty();
			}
		});
	};

	// --- 5. 이벤트 핸들러 및 IntersectionObserver 설정 ---
	container
		.on('click', '.interaction-buttons button[aria-label="좋아요"]', function(e) {
            e.preventDefault();
            if (!loginUserNo) {
                alert('로그인이 필요한 기능입니다.');
                $('#signin-modal').modal('show');
                return;
            }
            const $button = $(this);
            const feedSn = $button.closest('.feed-post-wrapper').data('feed-sn');
            
            $.post(`/customer/api/feeds/${feedSn}/like`)
                .done(function(result) {
                    $button.toggleClass('liked');
                    $button.closest('.feed-post-footer').find('.like-count').text(`좋아요 ${result.likeCount}개`);
                })
                .fail(function() {
                    alert('좋아요 처리에 실패했습니다.');
                });
        })
		.on('keypress', '.comment-input', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                const $input = $(this);
                const commentText = $input.val().trim();
                const feedSn = $input.closest('.feed-post-wrapper').data('feed-sn');

                if (!commentText) {
                    alert('댓글 내용을 입력해주세요.');
                    return;
                }
                if (!loginUserNo) {
                    alert('로그인이 필요한 기능입니다.');
                    $('#signin-modal').modal('show');
                    return;
                }

                $.post(`/customer/api/feeds/${feedSn}/comments`, { commentText: commentText })
                    .done(function(newComment) {
                        const newCommentHtml = createCommentHtml(newComment);
                        const $commentList = $input.closest('.feed-post').find('.comment-list');
                        $commentList.append(newCommentHtml);
                        $input.val('');
                        $commentList.closest('.post-scrollable-content').find('.no-comments-message').hide();
                        applyCommentActions();
                    })
                    .fail(function() {
                        alert('댓글 작성에 실패했습니다.');
                    });
            }
        })
		.on('click', '.delete-comment-btn', function(e) {
            e.preventDefault();
            if (!confirm('정말로 이 댓글을 삭제하시겠습니까?')) return;

            const $commentItem = $(this).closest('.comment-item');
            const commentSn = $commentItem.data('comment-sn');

            $.ajax({
                url: `/customer/api/feeds/comments/${commentSn}`,
                type: 'DELETE'
            })
            .done(function() {
                $commentItem.remove();
            })
            .fail(function() {
                alert('댓글 삭제에 실패했습니다.');
            });
        })
		.on('click', '.show-more-comments-btn', function(e) {
            e.preventDefault();
            const $button = $(this);
            $button.closest('.post-scrollable-content').find('.comment-item.hidden-comment').slice(0, 5).removeClass('hidden-comment');
            
            const remainingHidden = $button.closest('.post-scrollable-content').find('.comment-item.hidden-comment').length;
            if (remainingHidden > 0) {
                $button.text(`댓글 ${remainingHidden}개 더 보기`);
            } else {
                $button.parent().remove();
            }
        })
		.on('click', '.edit-comment-btn', function(e) {
            e.preventDefault();
            const $commentItem = $(this).closest('.comment-item');
            $commentItem.find('.comment-view-mode').hide();
            $commentItem.find('.comment-edit-mode').show();
        })
		.on('click', '.cancel-edit-btn', function(e) {
            e.preventDefault();
            const $commentItem = $(this).closest('.comment-item');
            $commentItem.find('.comment-edit-mode').hide();
            $commentItem.find('.comment-view-mode').show();
        })
		.on('click', '.save-edit-btn', function(e) {
            e.preventDefault();
            const $commentItem = $(this).closest('.comment-item');
            const commentSn = $commentItem.data('comment-sn');
            const commentText = $commentItem.find('.comment-edit-input').val().trim();

            if (!commentText) {
                alert('댓글 내용을 입력해주세요.');
                return;
            }

            $.ajax({
                url: `/customer/api/feeds/comments/${commentSn}`,
                type: 'PATCH',
                data: { commentText: commentText }
            })
            .done(function(updatedComment) {
                const updatedTimestamp = (updatedComment.mdfcnDt || updatedComment.crtDt).substring(0, 16);
                $commentItem.find('.comment-text').text(updatedComment.cmntCn);
                $commentItem.find('.comment-timestamp').text(updatedTimestamp + ' (수정됨)');
                $commentItem.find('.comment-edit-mode').hide();
                $commentItem.find('.comment-view-mode').show();
            })
            .fail(function() {
                alert('댓글 수정에 실패했습니다.');
            });
        });
    
    const observerTarget = document.getElementById('loading-indicator');
    if (observerTarget) {
        const observer = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && !isLoading && hasNext) {
                loadNextFeed(observer);

            }
        }, { 
            rootMargin: '200px'
        });
        observer.observe(observerTarget);
    }
    
    // --- 6. 초기 실행 ---
	applyCommentActions();
	initializeAllFollowButtons();
});
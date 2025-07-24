

$(() => {
	// --- 1. 전역 변수 및 초기 설정 ---
	const container = $('#feed-container');
	const loginUserNo = container.data('login-user-no');
	const INITIAL_COMMENT_COUNT = 5;
	let isLoading = false;
	let hasNext = true;

	// --- 2. 헬퍼 함수 (팔로우 버튼 초기화) ---
	/**
	 * 페이지에 있는 모든 피드 게시물의 팔로우 버튼을 초기화하는 함수.
	 * follow.js에 있는 initializeFollowButton 함수를 올바르게 호출합니다.
	 */
	function initializeAllFollowButtons() {
		// 아직 초기화되지 않은(.follow-initialized 클래스가 없는) 게시물 헤더를 모두 찾습니다.
		$('.feed-post-header:not(.follow-initialized)').each(function() {
			const $header = $(this);
			const $wrapper = $header.closest('.feed-post-wrapper');
			const writerUserNo = $wrapper.data('wrtr-user-no');

			// follow.js에 있는 전역 함수를 호출합니다.
			// 첫 번째 인자로, .follow-action-area를 포함하고 있는 $header를 전달합니다.
			// follow.js는 이 $header 안에서 .follow-action-area를 찾아 버튼을 생성합니다.
			if (typeof initializeFollowButton === 'function') {
				initializeFollowButton($header, writerUserNo, loginUserNo);
			} else {
				console.error('follow.js가 먼저 로드되지 않았거나, initializeFollowButton 함수를 찾을 수 없습니다.');
			}

			// 한 번 처리된 헤더에는 표시를 남겨 중복 실행을 방지합니다.
			$header.addClass('follow-initialized');
		});
	}

	// --- 3. 헬퍼 함수 (댓글, 피드 HTML 생성 및 UI 업데이트) ---
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
		const feedContentHtml = feed.feedCn ? `<div class="feed-content-wrapper"><div class="feed-content-body"><a href="${writerProfileLink}"><img src="${writerProfileImg}" alt="${writerNickname} 프로필" class="writer-profile-img"></a><p><a href="${writerProfileLink}" class="comment-writer-username">${writerNickname}</a> <span class="feed-text">${feed.feedCn}</span></p></div></div>` : '';
		const hashtagsHtml = (feed.tagList?.length > 0) ? `<div class="hashtags">${feed.tagList.map(tag => `<a href="#">#${tag.tagNm}</a>`).join(' ')}</div>` : '';
		const comments = feed.commentList || [];
		const commentListHtml = comments.map((comment, index) => createCommentHtml(comment, index >= INITIAL_COMMENT_COUNT ? 'hidden-comment' : '')).join('');
		const showMoreBtnHtml = (comments.length > INITIAL_COMMENT_COUNT) ? `<div class="show-more-wrapper"><button class="show-more-comments-btn">댓글 ${comments.length - INITIAL_COMMENT_COUNT}개 더 보기</button></div>` : '';
		const postTimestamp = feed.crtDt ? feed.crtDt.substring(0, 16) : '';
		const followActionHtml = (loginUserNo && loginUserNo != feed.wrtrUserNo) ? `<div class="follow-action-area"></div>` : '';
		const postActionsHtml = `<button class="options-menu-btn" aria-label="옵션 더 보기"><svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><circle cx="12" cy="12" r="1.5"></circle><circle cx="6" cy="12" r="1.5"></circle><circle cx="18" cy="12" r="1.5"></circle></svg></button>`;
		return `<div class="feed-post-wrapper" data-crt-dt="${feed.crtDt}" data-wrtr-user-no="${feed.wrtrUserNo}" data-feed-sn="${feed.feedSn}"><section class="post-media-section"><div class="feed-post-content">${imageListHtml}</div></section><section class="post-info-section"><article class="feed-post"><header class="feed-post-header"><div class="user-profile"><a href="${writerProfileLink}"><img src="${writerProfileImg}" alt="프로필 사진" class="profile-image"></a><div class="user-info"><span class="username">${writerNickname}</span></div>${followActionHtml}</div><div class="post-actions">${postActionsHtml}</div></header><div class="post-scrollable-content">${feedContentHtml}${hashtagsHtml}<ul class="comment-list">${commentListHtml}</ul>${showMoreBtnHtml}</div><footer class="feed-post-footer"><div class="interaction-buttons"><button aria-label="좋아요"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"></path></svg></button><button aria-label="댓글"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"></path></svg></button><button aria-label="공유"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3s3-1.34 3-3-1.34-3-3-3z"></path></svg></button></div><p class="like-count">좋아요 ${feed.likeCount}개</p><p class="post-timestamp">${postTimestamp}</p><div class="comment-input-area"><input type="text" placeholder="댓글 남기기..." class="comment-input"></div></footer></article></section></div>`;
	};

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

	// --- 4. 무한 스크롤 및 이벤트 핸들러 ---
	const loadNextFeed = () => {
		if (isLoading || !hasNext) return;
		isLoading = true;
		$('#loading-indicator').show();
		const lastFeedWrapper = $('.feed-post-wrapper').last();
		const currentFeedCrtDt = lastFeedWrapper.data('crt-dt');
		if (!currentFeedCrtDt) {
			isLoading = false;
			$('#loading-indicator').hide();
			return;
		}

		$.ajax({ url: '/customer/api/feeds/next', type: 'GET', data: { currentFeedCrtDt, limit: 3, context: container.data('context'), userNo: container.data('user-no') } })
			.done(nextFeedList => {
				if (nextFeedList && nextFeedList.length > 0) {
					nextFeedList.forEach(nextFeed => container.append(createFeedDetailHtml(nextFeed)));
					applyCommentActions();
					initializeAllFollowButtons(); // 새로 추가된 피드에 대해서도 팔로우 버튼 생성
				} else {
					hasNext = false;
					$('#loading-indicator').html('<p>마지막 피드입니다.</p>').show();
				}
			}).fail(err => {
				console.error("다음 피드 로딩 실패:", err);
				hasNext = false;
			}).always(() => {
				isLoading = false;
				if (hasNext) $('#loading-indicator').hide();
			});
	};

	$(window).on('scroll', () => {
		if ($(window).scrollTop() + $(window).height() >= $('body').get(0).scrollHeight - 200) {
			loadNextFeed();
		}
	});

	container
		.on('click', '.interaction-buttons button[aria-label="좋아요"]', function(e) {
			e.preventDefault();
			if (!loginUserNo) {
				$('#signin-modal').modal('show');
				return;
			}
			const likeButton = $(this);
			const wrapper = likeButton.closest('.feed-post-wrapper');
			const feedSn = wrapper.data('feed-sn');
			if (!feedSn) return;
			likeButton.prop('disabled', true);
			$.ajax({ url: `/customer/api/feeds/${feedSn}/like`, type: 'POST' })
				.done(response => {
					likeButton.addClass('liked');
					wrapper.find('.like-count').text(`좋아요 ${response.likeCount}개`);
				})
				.fail(jqXHR => {
					if (jqXHR.status === 401) { $('#signin-modal').modal('show'); }
					else { alert('요청에 실패했습니다.'); }
				}).always(() => {
					likeButton.prop('disabled', false);
				});
		})
		.on('keypress', '.comment-input', function(e) {
			if (e.which === 13) {
				e.preventDefault();
				if (!loginUserNo) {
					$('#signin-modal').modal('show');
					return;
				}
				const inputField = $(this);
				const commentText = inputField.val().trim();
				const wrapper = inputField.closest('.feed-post-wrapper');
				const feedSn = wrapper.data('feed-sn');
				if (commentText === '' || !feedSn) return;
				inputField.prop('disabled', true);
				$.ajax({ url: `/customer/api/feeds/${feedSn}/comments`, type: 'POST', data: { commentText } })
					.done(newComment => {
						const newCommentHtml = createCommentHtml(newComment);
						wrapper.find('.comment-list').append(newCommentHtml);
						inputField.val('');
						applyCommentActions();
					})
					.fail(jqXHR => {
						if (jqXHR.status === 401) { $('#signin-modal').modal('show'); }
						else { alert('댓글 작성에 실패했습니다.'); }
					}).always(() => {
						inputField.prop('disabled', false);
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
					if (jqXHR.status === 401) { $('#signin-modal').modal('show'); }
					else { alert('댓글 삭제에 실패했습니다.'); }
				});
		})
		.on('click', '.show-more-comments-btn', function(e) {
			e.preventDefault();
			$(this).closest('.post-scrollable-content').find('.hidden-comment').removeClass('hidden-comment');
			$(this).parent().remove();
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
					if (jqXHR.status === 401) { $('#signin-modal').modal('show'); }
					else { alert('댓글 수정에 실패했습니다.'); }
				});
		});

	// --- 5. 초기 실행 ---
	applyCommentActions();
	initializeAllFollowButtons(); // 페이지가 처음 로드될 때 팔로우 버튼을 초기화합니다.
});
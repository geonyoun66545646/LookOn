$(() => {
    const container = $('#feed-container');
    const context = container.data('context');
    let isLoading = false;
    let hasNext = true;

    /**
     * 서버에서 받은 피드(JSON) 1개를 2단 레이아웃 HTML 구조로 변환합니다.
     */
    const createFeedDetailHtml = (feed) => {
        const writerNickname = feed.writerInfo?.userNcnm || '알 수 없는 사용자';
        const writerProfileImg = feed.writerInfo?.prflImg || '/uploads/profiles/defaultprofile.jpg';
        const writerProfileLink = `/customer/feed/feedListByUserNo/${feed.wrtrUserNo}`;
        const imageListHtml = feed.imageList?.map(image => `<img src="${image.imgFilePathNm}" alt="피드 이미지" class="main-feed-image">`).join('') || '';
        const feedContentHtml = feed.feedCn ? `<div class="feed-content-body"><a href="${writerProfileLink}"><img src="${writerProfileImg}" alt="${writerNickname} 프로필" class="writer-profile-img"></a><p><a href="${writerProfileLink}" class="comment-writer-username">${writerNickname}</a> <span class="feed-text">${feed.feedCn}</span></p></div>` : '';
        let hashtagsHtml = '';
        if (feed.tagList?.length > 0) {
            const tagLinks = feed.tagList.map(tag => `<a href="#">#${tag.tagNm}</a>`).join(' ');
            hashtagsHtml = `<div class="hashtags">${tagLinks}</div>`;
        }
        let commentListHtml = '';
        if (feed.commentList?.length > 0) {
            commentListHtml = feed.commentList.map(comment => createCommentHtml(comment)).join('');
        }
        // [핵심 수정] 게시물 타임스탬프 형식 변경
        const postTimestamp = feed.crtDt ? feed.crtDt.substring(0, 16) : '';

        return `<div class="feed-post-wrapper" data-crt-dt="${feed.crtDt}" data-wrtr-user-no="${feed.wrtrUserNo}" data-feed-sn="${feed.feedSn}"><section class="post-media-section"><div class="feed-post-content">${imageListHtml}</div></section><section class="post-info-section"><article class="feed-post"><header class="feed-post-header"><div class="user-profile"><a href="${writerProfileLink}"><img src="${writerProfileImg}" alt="프로필 사진" class="profile-image"></a><div class="user-info"><span class="username">${writerNickname}</span></div></div><div class="post-actions"><button class="more-options-btn" aria-label="더보기"><svg height="24" viewBox="0 0 24 24" width="24"><circle cx="12" cy="12" r="1.5"></circle><circle cx="6" cy="12" r="1.5"></circle><circle cx="18" cy="12" r="1.5"></circle></svg></button></div></header><div class="post-scrollable-content"><ul class="comment-list">${feedContentHtml}${hashtagsHtml}${commentListHtml}</ul></div><footer class="feed-post-footer"><div class="interaction-buttons"><button aria-label="좋아요"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"></path></svg></button><button aria-label="댓글"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"></path></svg></button><button aria-label="공유"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3s3-1.34 3-3-1.34-3-3-3z"></path></svg></button></div><p class="like-count">좋아요 ${feed.likeCount}개</p><p class="post-timestamp">${postTimestamp}</p><div class="comment-input-area"><input type="text" placeholder="댓글 남기기..." class="comment-input"></div></footer></article></section></div>`;
    };
    
    const createCommentHtml = (comment) => {
        const writerProfileImg = comment.writerInfo?.prflImg || '/uploads/profiles/defaultprofile.jpg';
        const writerNickname = comment.writerInfo?.userNcnm || '작성자';
        const writerLink = `/customer/feed/feedListByUserNo/${comment.writerInfo?.userNo}`;
        // [핵심 수정] 댓글 타임스탬프 형식 변경
        const commentTimestamp = comment.crtDt ? comment.crtDt.substring(0, 16) : '';

        return `<li class="comment-item"><div class="comment-writer-profile"><a href="${writerLink}"><img src="${writerProfileImg}" alt="프로필 사진"></a></div><div class="comment-content"><p><a href="${writerLink}" class="comment-writer-username">${writerNickname}</a> <span class="comment-text">${comment.cmntCn}</span></p><span class="comment-timestamp">${commentTimestamp}</span></div></li>`;
    };

    const loadNextFeed = () => {
        if (isLoading || !hasNext) return;
        const lastFeedWrapper = $('.feed-post-wrapper').last();
        const currentFeedCrtDt = lastFeedWrapper.data('crt-dt');
        const userNo = container.data('user-no');
        if (!currentFeedCrtDt) return;
        isLoading = true;
        $('#loading-indicator').show();
        $.ajax({ url: '/customer/api/feeds/next', type: 'GET', data: { currentFeedCrtDt, limit: 3, context, userNo }, dataType: 'json' })
        .done(nextFeedList => {
            if (nextFeedList?.length > 0) {
                nextFeedList.forEach(nextFeed => container.append(createFeedDetailHtml(nextFeed)));
            } else {
                hasNext = false;
                $('#loading-indicator').html('<p>마지막 피드입니다.</p>').show();
            }
        })
        .fail(err => { console.error("다음 피드 로딩 실패:", err); hasNext = false; })
        .always(() => { isLoading = false; if (hasNext) $('#loading-indicator').hide(); });
    };

    const onScroll = () => {
        if ($(window).scrollTop() + $(window).height() >= $('body').get(0).scrollHeight - 200) {
            loadNextFeed();
        }
    };
    $(window).on('scroll', onScroll);

    container.on('click', '.interaction-buttons button[aria-label="좋아요"]', function(e) {
        e.preventDefault();
        const likeButton = $(this);
        if (likeButton.hasClass('liked')) return;
        const wrapper = likeButton.closest('.feed-post-wrapper');
        const feedSn = wrapper.data('feed-sn');
        if (!feedSn) return;
        $.ajax({
            url: `/customer/api/feeds/${feedSn}/like`, type: 'POST'
        })
        .done(response => {
            likeButton.addClass('liked');
            wrapper.find('.like-count').text(`좋아요 ${response.likeCount}개`);
        })
        .fail(jqXHR => {
            if (jqXHR.status === 401) {
                alert('로그인이 필요합니다.');
                window.location.href = '/customer/login/login?redirectUrl=' + window.location.pathname;
            } else { alert('요청에 실패했습니다. 잠시 후 다시 시도해주세요.'); }
        });
    });

    container.on('keypress', '.comment-input', function(e) {
        if (e.which === 13) {
            e.preventDefault();
            const inputField = $(this);
            const commentText = inputField.val().trim();
            const wrapper = inputField.closest('.feed-post-wrapper');
            const feedSn = wrapper.data('feed-sn');
            if (commentText === '' || !feedSn) return;
            $.ajax({
                url: `/customer/api/feeds/${feedSn}/comments`, type: 'POST', data: { commentText: commentText }
            })
            .done(newComment => {
                const newCommentHtml = createCommentHtml(newComment);
                wrapper.find('.comment-list').append(newCommentHtml);
                inputField.val('');
            })
            .fail(jqXHR => {
                if (jqXHR.status === 401) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/customer/login/login?redirectUrl=' + window.location.pathname;
                } else { alert('댓글 작성에 실패했습니다. 다시 시도해주세요.'); }
            });
        }
    });
});
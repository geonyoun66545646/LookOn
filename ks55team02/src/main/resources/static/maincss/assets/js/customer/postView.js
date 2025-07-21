$(document).ready(function() {
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

    $('.relative-time').each(function() {
        const datetime = $(this).data('datetime');
        if (datetime) {
            $(this).text(formatRelativeTime(datetime));
        }
    });

    $('.btn-more').on('click', function(e) {
        e.stopPropagation();
        const $menu = $(this).siblings('.more-menu');
        $('.more-menu').not($menu).hide();
        $menu.toggle();
    });

    $(document).on('click', function() {
        $('.more-menu').hide();
    });

    $('#delete-post-btn').on('click', function(e) {
        if (!confirm('정말로 이 게시글을 삭제하시겠습니까?')) return;
        const deleteUrl = $(e.currentTarget).data('post-delete-url');
        $.ajax({
            url: deleteUrl,
            type: 'DELETE',
            dataType: 'json'
        }).done(response => {
            alert(response.message || '게시글이 삭제되었습니다.');
            location.href = '/customer/post/postList';
        }).fail(err => {
            alert(err.responseJSON?.message || '삭제 중 오류가 발생했습니다.');
        });
    });

    // [수정] 추천(좋아요) 토글 기능 -> 이벤트 위임 방식으로 변경
    $('.post-view-container').on('click', '#post-like-btn', function() {
        const $btn = $(this);
        const pstSn = $btn.data('post-sn');
        const $container = $('.post-view-container');
        const loginUserNo = $container.data('login-user-no');

        if (!loginUserNo) {
            alert('로그인이 필요합니다.');
            const loginUrl = '/customer/login/login?redirectUrl=' + window.location.pathname + window.location.search;
            window.location.href = loginUrl;
            return;
        }

        $btn.prop('disabled', true);

        $.ajax({
            url: '/customer/api/post/toggle-like',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ pstSn: pstSn }),
            dataType: 'json'
        }).done(response => {
            $btn.toggleClass('liked', response.isLiked);
            $btn.find('.like-count').text(response.likeCount);
        }).fail(err => {
            alert(err.responseJSON?.message || '오류가 발생했습니다.');
        }).always(() => {
            $btn.prop('disabled', false);
        });
    });

    $('#post-share-btn').on('click', () => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(window.location.href).then(() => {
                alert('게시글 주소가 클립보드에 복사되었습니다.');
            }).catch(err => {
                alert('주소 복사에 실패했습니다. 다시 시도해주세요.');
            });
        } else {
            alert('이 브라우저에서는 자동 복사를 지원하지 않습니다.');
        }
    });

    $('.report-btn').on('click', () => {
        alert('신고 기능은 현재 준비 중입니다.');
    });
    
    const $commentTextarea = $('#comment-textarea');
    const $commentSubmitBtn = $('#comment-submit-btn');
    if ($commentTextarea.length) {
        $commentTextarea.on('input', function() {
            $commentSubmitBtn.prop('disabled', $(this).val().trim() === '');
        });
    }

    $('#commentForm').on('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);
        const formObject = Object.fromEntries(formData.entries());
        $.ajax({
            url: '/customer/api/post/comments',
            method: 'POST',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(formObject),
            dataType: 'json'
        }).done(response => {
            if (response.result === 'success') {
                window.location.reload();
            } else {
                alert(response.message || '댓글 등록에 실패했습니다.');
            }
        }).fail(err => {
            alert(err.responseJSON?.message || '댓글 작성 중 오류가 발생했습니다.');
        });
    });

    $('.comment-list').on('click', '.update-comment-btn', function() {
        const $commentItem = $(this).closest('.comment-item');
        $commentItem.find('.comment-body, .update-comment-btn, .delete-comment-btn').hide();
        $commentItem.find('.comment-update-form, .save-comment-btn, .cancel-comment-btn').show();
        $commentItem.find('.comment-update-input').focus();
    });

    $('.comment-list').on('click', '.cancel-comment-btn', function() {
        const $commentItem = $(this).closest('.comment-item');
        $commentItem.find('.comment-update-form, .save-comment-btn, .cancel-comment-btn').hide();
        $commentItem.find('.comment-body, .update-comment-btn, .delete-comment-btn').show();
    });

    $('.comment-list').on('click', '.save-comment-btn', function(e) {
        const $btn = $(e.currentTarget);
        const updateUrl = $btn.data('comment-update-url');
        const cmntCn = $btn.closest('.comment-item').find('.comment-update-input').val();
        if (!cmntCn.trim()) {
            alert('수정할 내용을 입력해주세요.');
            return;
        }
        $.ajax({
            url: updateUrl,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({ cmntCn: cmntCn }),
            dataType: 'json'
        }).done(response => {
            window.location.reload();
        }).fail(err => {
            alert(err.responseJSON?.message || '댓글 수정 중 오류가 발생했습니다.');
        });
    });

    $('.comment-list').on('click', '.delete-comment-btn', function(e) {
        if (!confirm('정말로 이 댓글을 삭제하시겠습니까?')) return;
        const deleteUrl = $(e.currentTarget).data('comment-delete-url');
        $.ajax({
            url: deleteUrl,
            type: 'DELETE',
            dataType: 'json'
        }).done(response => {
            window.location.reload();
        }).fail(err => {
            alert(err.responseJSON?.message || '댓글 삭제 중 오류가 발생했습니다.');
        });
    });
});
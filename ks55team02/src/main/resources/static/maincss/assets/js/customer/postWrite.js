/**
 * postWrite.js
 * 게시글 작성/수정 폼의 AJAX 처리
 */

$(() => {
	// 게시글 작성 ajax 스크립트
    const $postForm = $('#postForm');

    if($postForm.length) {
        $postForm.on('submit', e => {
            e.preventDefault();

            $.ajax({
                url: $postForm.attr('action'),
                method: $postForm.attr('method'),
                data: $postForm.serialize()
            }).done(result => {
                window.location.href = '/customer/post/postList';
            }).fail(error => {
                console.error('AJAX 실패:', error);
                alert('글 작성중 오류 발생');
            });
        });
    }
});
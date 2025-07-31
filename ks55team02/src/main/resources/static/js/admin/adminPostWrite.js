$(() => {
    const simplemde = new SimpleMDE({ 
        element: $("#postContent")[0],
        spellChecker: false,
        placeholder: "내용을 입력하세요...",
    });

    $('#post-write-form').on('submit', function(e) {
        e.preventDefault();

        const postData = {
            // [수정] 드롭다운에서 선택된 값을 가져옵니다.
            boardClassCode: $('#boardCategorySelect').val(),
            postTitle: $('#postTitle').val().trim(),
            postContent: simplemde.value().trim(),
        };

        // [추가] 유효성 검사
        if (!postData.boardClassCode) {
            alert('카테고리를 선택해주세요.');
            return;
        }
        if (!postData.postTitle || !postData.postContent) {
            alert('제목과 내용을 모두 입력해주세요.');
            return;
        }

        $.ajax({
            url: '/adminpage/boardadmin/postManagement/write',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(postData)
        })
        .done(function() {
            alert('게시글이 성공적으로 등록되었습니다.');
            location.href = '/adminpage/boardadmin/postManagement';
        })
        .fail(function(xhr) {
            const errorMessage = xhr.responseJSON?.message || '게시글 등록 중 오류가 발생했습니다.';
            alert(errorMessage);
        });
    });
});
/**
 * postVist.js
 * 게시글 상세 페이지의 게시글 및 댓글 관련 기능 처리 (AJAX 기반)
 */

$(() => {
	// 게시글 삭제
    $('#delete-post-btn').on('click', e => {
        e.preventDefault();
        
        if(!confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
            return;
        }

        const deleteUrl = $(e.currentTarget).data('post-delete-url');

        $.ajax({
            url: deleteUrl,
            type: 'DELETE'
        }).done(result => {
            location.href = '/customer/post/postList';
        }).fail(error => {
            console.error("삭제 실패: ", error);
            alert('삭제에 실패했습니다.');
        });
    });
    
	// 댓글 작성
    const $commentForm = $('#commentForm');
    if($commentForm.length) {
    	$commentForm.on('submit', e => {
    		e.preventDefault();
    		
	    	const formData = $commentForm.serialize(); 
    		
    		$.ajax({
    			url: '/customer/post/insertComment',
    			method: 'Post',
    			data: formData,
    			dataType: 'json'
    		}).done(response => {
    			if(response.result === "success") {
    				window.location.reload();
    			} else {
    				alert(response);
    			}
    		}).fail(() => {
    			alert('댓글 작성중 오류 발생')
    		});
    	});
    }
    
    // 댓글 수정 UI 오픈
    $('.update-comment-btn').on('click', e => {
    	e.preventDefault();
    	
    	const $commentItem = $(e.currentTarget).closest('.comment-item');
    	
    	$commentItem.find('.comment-content-display').hide();
    	$commentItem.find('.comment-update-input').show();
    	$(e.currentTarget).hide();
    	$commentItem.find('.save-comment-btn, .cancel-comment-btn').show();
    });
    
    // 댓글 수정 UI 클로즈
    $('.cancel-comment-btn').on('click', e => {
    	e.preventDefault();
    	
    	const $commentItem = $(e.currentTarget).closest('.comment-item');
    	
    	$commentItem.find('.comment-content-display').show();
    	$commentItem.find('.comment-update-input').hide();
    	$commentItem.find('.update-comment-btn').show();
    	$commentItem.find('.save-comment-btn, .cancel-comment-btn').hide();
    });
    
    // 댓글 수정
	$('.save-comment-btn').on('click', e => {
	    e.preventDefault();
	    
	    const $btn = $(e.currentTarget);
	    const updateUrl = $btn.data('comment-update-url');
	    
	    // 수정 1: '저장' 버튼의 data-comment-sn 속성에서 댓글 고유 번호를 가져옵니다.
	    const pstCmntSn = $btn.data('comment-sn');
	    
	    // 수정 2: 클릭된 버튼을 기준으로 가장 가까운 .comment-item 요소를 찾고,
	    // 그 안에서 수정 중인 textarea를 찾아 .val()로 실제 입력된 값을 가져옵니다.
	    const $commentItem = $btn.closest('.comment-item');
	    const cmntCn = $commentItem.find('.comment-update-input').val();
	    
	    // 수정 3: 서버의 Comment 객체 필드명과 일치하는 JSON 데이터를 생성합니다.
	    // (pstCmntSn: 댓글 고유번호, cmntCn: 수정된 내용)
	    const commentData = {
	        pstCmntSn: pstCmntSn,
	        cmntCn: cmntCn
	    };
	    
	    // 이 부분은 기존 코드와 동일하며, 정상적으로 동작합니다.
	    $.ajax({
	        url: updateUrl,
	        type: 'POST',
	        contentType: 'application/json',
	        data: JSON.stringify(commentData),
	        dataType: 'json'
	    }).done(response => {
	        if(response.result === "success") {
	            window.location.reload();			    			
	        } else {
	            alert("댓글 수정 실패 : " + (response.message || '알수없는오류'));
	        }
	    }).fail(error => {
	        console.error("수정 실패", error);
	        alert('수정에 실패했습니다.');
	    });
	});
    
    // 댓글 삭제
    $('.delete-comment-btn').on('click', e => {
    	e.preventDefault();
    	
    	if(!confirm('정말로 댓글을 삭제하시겠습니까?')) {
    		return;
    	}
    	
    	const deleteUrl = $(e.currentTarget).data('comment-delete-url');
    	
    	$.ajax({
    		url: deleteUrl,
    		type: 'DELETE'
    	}).done(result => {
    		window.location.reload();
    	}).fail(error => {
            console.error("삭제 실패: ", error);
            alert('삭제에 실패했습니다.');
    	});
    });
    
	 // 추천수 증가
    $('#post_interaction_btn').on('click', e => {
        e.preventDefault();
        
        const $btn = $(e.currentTarget);
        const insertUrl = $btn.data('post-interaction-insert-url');
        // 수정 1: HTML의 data-* 속성 이름은 소문자로 접근하는 것이 표준입니다. (interaction-pstsn -> interactionPstsn)
        const pstSn = $btn.data('interaction-pstsn');
        const userNo = $btn.data('interaction-userno');
        
        $.ajax({
            url: insertUrl,
            type: 'POST',
            data: {
                pstSn: pstSn,
                userNo: userNo
            },
            dataType: 'json' // 수정 2: 서버가 JSON 형태로 응답하므로 dataType을 추가합니다.
        }).done(response => {
            // 수정 3: 서버의 응답 결과에 따라 분기 처리합니다.
            if (response.result === "success") {
                // 성공 시 페이지를 새로고침하여 추천 수를 즉시 반영합니다.
                window.location.reload();
            } else {
                // 실패 시 서버가 보낸 메시지를 알림창에 표시합니다.
				console.log("추천 실패");
            }
        }).fail(error => {
            console.error("추천 실패", error);
        });
    });
});

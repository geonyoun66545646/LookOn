/**
 * 
 */

$('#login-btn').on('click', (e) => {
    // 폼의 기본 전송 기능(새로고침)을 막습니다.
    e.preventDefault();

    const loginData = {
        userLgnId: $('#login-id').val(),
        userPswdEncptVal: $('#login-pw').val()
    };

    // 간단한 유효성 검사
    if (!loginData.userLgnId || !loginData.userPswdEncptVal) {
        alert('아이디와 비밀번호를 모두 입력해주세요.');
        return;
    }
	$('#signin-modal').on('click', '#logout-btn', e => {
	    // 1. 다른 스크립트의 방해를 막기 위해 기본 동작과 이벤트 전파를 중단
	    e.preventDefault();
	    e.stopPropagation();

	    // 2. 클릭된 #logout-btn <a> 태그의 href 속성 값을 가져옴
	    const logoutUrl = $(e.currentTarget).attr('href');
	    
	    // 3. href 값이 존재하면, 해당 URL로 페이지를 강제 이동
	    if (logoutUrl) {
	        window.location.href = logoutUrl;
	    } else {
	        // 만약의 경우를 대비한 에러 로그
	        console.error('로그아웃 버튼(#logout-btn)에 href 속성이 정의되지 않았습니다.');
	    }
	});

    $.ajax({
        url: '/customer/login', // 서버의 로그인 처리 URL
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(loginData),
        success: (response) => {
            console.log(response); // 서버 응답 확인용 로그
            
            if (response.status === 'success') {
                alert(response.message);
				const redirectUrl = $('#redirectUrl').val();
                // 로그인 성공 후 메인 페이지 등으로 이동
                window.location.href = redirectUrl ? redirectUrl : '/'; 
            } else {
                // 실패 메시지(잠김, 휴면, 불일치 등)를 사용자에게 알림
                alert(response.message);
            }
        },
        error: (xhr, status, error) => {
            console.error("로그인 요청 실패: ", error);
            alert("로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    });
});
/**
 * 
 */

$('#login-btn').on('click', (e) => {
    e.preventDefault();

    // [수정 후] 로그인 데이터에 redirectUrl을 포함시킵니다.
    const loginData = {
        userLgnId: $('#login-id').val(),
        userPswdEncptVal: $('#login-pw').val(),
        redirectUrl: $('#redirectUrl').val() // 숨겨진 필드의 값을 가져와 추가
    };

    if (!loginData.userLgnId || !loginData.userPswdEncptVal) {
        alert('아이디와 비밀번호를 모두 입력해주세요.');
        return;
    }

    $.ajax({
        url: '/customer/login',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(loginData),
		success: (response) => {
		    console.log(response); // 서버 응답을 콘솔에서 확인하는 것은 디버깅에 좋으니 그대로 둡니다.

		    // 1. 로그인 성공 시
		    if (response.status === 'success') {
		        alert(response.message);
		        // 우리 프로젝트의 메인 페이지는 '/main' 이므로 기본값을 수정합니다.
		        window.location.href = response.redirectUrl ? response.redirectUrl : '/main';

		    // 2. 그 외 모든 실패 및 예외 케이스 처리
		    } else {
		        // 'fail', 'locked', 'inactive', 'already_logged_in' 등 모든 경우
		        // 서버가 보내주는 message를 그대로 alert 창으로 띄워줍니다.
		        alert(response.message);
		    }
		},
        error: (xhr, status, error) => {
            console.error("로그인 요청 실패: ", error);
            alert("로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    });
});
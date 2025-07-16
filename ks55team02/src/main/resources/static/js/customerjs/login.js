/**
 * 
 */
$(document).ready(function() {
    
    // 1. 현재 페이지의 URL에서 파라미터 정보를 읽어옵니다.
    const urlParams = new URLSearchParams(window.location.search);
    
    // 2. URL에 'showLogin=true' 라는 신호가 있는지 확인합니다.
    if (urlParams.get('showLogin') === 'true') {
        
        // 3. 신호가 있다면, URL에서 아이디와 비밀번호 값을 가져옵니다.
        const userId = urlParams.get('id');
        const userPw = urlParams.get('pw');

        // 4. 로그인 모달의 ID는 "signin-modal" 입니다.
        const $loginModal = $('#signin-modal');
        
        // 5. 가져온 아이디와 비밀번호 값을 로그인 모달의 각 input 창에 채워넣습니다.
        //    (보내주신 모달 HTML을 보니, 아이디 input의 id는 'login-id', 비밀번호는 'login-pw'가 맞습니다.)
        if (userId) {
            $loginModal.find('#login-id').val(userId);
        }
        if (userPw) {
            $loginModal.find('#login-pw').val(userPw);
        }
        
        // 6. Bootstrap 모달을 여는 정확한 코드를 사용합니다.
        $loginModal.modal('show');
        
        // 7. 사용자가 새로고침해도 모달이 다시 뜨지 않도록, URL에서 파라미터를 깔끔하게 제거합니다.
        history.replaceState(null, null, window.location.pathname);
    }
});
const savedUserId = $.cookie('saved_user_id');

// 2. 쿠키에 아이디가 저장되어 있다면,
if (savedUserId) {
    $('#login-id').val(savedUserId); // 아이디 입력창에 값을 채워넣고,
    $('#signin-remember').prop('checked', true); // '아이디 저장' 체크박스를 체크 상태로 둔다.
}

$('#login-id, #login-pw').on('keypress', function(e) {
    if (e.which === 13) {
        e.preventDefault(); // 폼 기본 제출 방지
        $('#login-btn').trigger('click'); // 로그인 버튼 클릭!
    }
});

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
	
	if ($('#signin-remember').is(':checked')) {
	        // 체크박스가 체크되어 있다면, 쿠키에 아이디를 30일간 저장한다.
	        $.cookie('saved_user_id', $('#login-id').val(), { expires: 30 });
	    } else {
	        // 체크되어 있지 않다면, 쿠키를 삭제한다.
	        $.removeCookie('saved_user_id');
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
		        window.location.href = response.redirectUrl ? response.redirectUrl : '/main';

		    // 2. 그 외 모든 실패 및 예외 케이스 처리
		    } else {
		        // 'fail', 'locked', 'inactive', 'already_logged_in' 등 모든 경우
		        // 서버가 보내주는 message를 그대로 alert 창으로 띄워줍니다.
		        alert(response.message);
				$('#login-id').val('').focus();
		    }
		},
        error: (xhr, status, error) => {
            console.error("로그인 요청 실패: ", error);
            alert("로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    });
});
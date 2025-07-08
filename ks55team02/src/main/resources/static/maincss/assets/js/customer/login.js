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
            console.log(response);
            
            if (response.status === 'success') {
                alert(response.message);
                
                // [수정 후] 서버가 돌려준 redirectUrl을 사용합니다. 없으면 기본값 '/' 사용.
                window.location.href = response.redirectUrl ? response.redirectUrl : '/';

            } else {
                alert(response.message);
            }
        },
        error: (xhr, status, error) => {
            console.error("로그인 요청 실패: ", error);
            alert("로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    });
});
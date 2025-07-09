/**
  * 로그인 인터셉터에 의해 리다이렉트된 경우,
  * 알림 및 로그인 모달을 처리하는 스크립트.
  * HTML의 body 태그에 있는 data-* 속성에서 값을 읽어온다. 
 */
$(() => {
    // body 태그에 심어놓은 데이터 속성 값을 가져온다.
    const authAlertMessage = $('body').data('auth-alert');
    
    // data- 속성 값이 존재하고, 비어있지 않은 경우에만 실행
    if (authAlertMessage) {
        // 1. 메시지가 있으면 경고창으로 보여주기
        alert(authAlertMessage);
        
        // 2. 로그인 모달창 띄우기
        $('#signin-modal').modal('show');
        
        // 3. 원래 가려던 URL을 로그인 폼의 hidden input에 넣어주기
        const redirectUrl = $('body').data('redirect-url');
        if (redirectUrl) {
            $('#signin-modal #redirectUrl').val(redirectUrl);
        }
    }
});
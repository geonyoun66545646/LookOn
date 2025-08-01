/**
 * =======================================================================
 * 회원정보 수정 페이지 (editProfile.html) 전용 스크립트
 * -----------------------------------------------------------------------
 * 이 파일은 회원정보 수정, 프로필 관리, 보안 설정 탭의 모든 기능을 담당합니다.
 * IIFE(즉시 실행 함수) 패턴을 사용하여 전역 스코프의 오염을 방지합니다.
 * =======================================================================
 */
(function($) {
    "use strict"; // 엄격 모드로 코드 오류를 줄입니다.

    // =======================================================================
    // 1. 상수 선언 (Constants)
    // =======================================================================
    
    // 비밀번호 정책 정규식 (8~20자, 영문/숫자/특수문자 각각 1개 이상 포함)
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()])[a-zA-Z\d!@#$%^&*()]{8,20}$/;


    // =======================================================================
    // 2. 초기화 및 이벤트 바인딩 (Initialization & Event Binding)
    // =======================================================================

    // 페이지 로딩이 완료되면 실행될 메인 함수
    function initializePage() {
        loadMyInfo();
        bindEvents();
        activateTabFromUrl();
    }

    // 모든 이벤트 리스너를 한 곳에서 관리하는 함수
    function bindEvents() {
        
        // --- 탭 1: 회원정보 수정 관련 이벤트 ---
        $('#userEmail').on('keyup blur', function() { validateEmail($(this).val()); });
        $('#userTelNo').on('keyup blur', function() { validateTelno($(this).val()); });
        $('.address-group button').on('click', searchZipCode);
        $('#edit-info form').on('submit', updateUserInfo);

        // --- 탭 2: 프로필 관리 관련 이벤트 ---
        $('#profile-update-form').on('submit', updateProfileInfo);
        
		/*
        $('#userNickname').on('blur', function() {
            const nickname = $(this).val();
            const $errorElement = $('#nicknameError');
            const nicknameRegex = /^[a-zA-Z0-9가-힣]{2,15}$/;

            if (nickname === '') {
                $errorElement.hide();
                return;
            }
            if (!nicknameRegex.test(nickname)) {
                $errorElement.text('닉네임은 2~15자리의 한글, 영문, 숫자만 사용 가능합니다.').show();
                return;
            }

            $.ajax({
                type: 'GET',
                url: '/api/v1/mypage/profile/check-nickname',
                data: { 'userNcnm': nickname }
            })
            .done(function(response) {
                if (!response.isAvailable) {
                     $errorElement.text('사용 가능한 닉네임입니다.').css('color', 'green').show();    
                } else {
                    $errorElement.text('이미 사용 중인 닉네임입니다.').show();    
                }
            })
            .fail(function(error) {
                console.error("닉네임 중복 확인 중 에러 발생:", error);
                $errorElement.text('닉네임 확인 중 오류가 발생했습니다.').show();
            });
        });
		*/

        $('#profileImageInput').on('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                $(this).next('.custom-file-label').html(file.name);
                const reader = new FileReader();
                reader.onload = function(event) {
                    $('#profileImagePreview').attr('src', event.target.result).show();
                }
                reader.readAsDataURL(file);
            }
        });

        // --- 탭 3: 보안 설정 관련 이벤트 ---
        $('#password-change-form').on('submit', changePassword);

        $('#newPassword').on('keyup blur', function() {
            const newPassword = $(this).val();
            const $errorElement = $('#newPasswordError');
            if (newPassword === '') {
                showFeedback($errorElement, '', true);
                return;
            }
            if (!passwordRegex.test(newPassword)) {
                showFeedback($errorElement, '8~20자, 영문/숫자/특수문자(!@#$%^&*()) 조합', false);
            } else {
                showFeedback($errorElement, '안전', true);
            }
            $('#confirmNewPassword').trigger('keyup');
        });

        $('#confirmNewPassword').on('keyup blur', function() {
            const newPassword = $('#newPassword').val();
            const confirmNewPassword = $(this).val();
            const $errorElement = $('#confirmNewPasswordError');
            if (confirmNewPassword === '') {
                showFeedback($errorElement, '', true);
                return;
            }
            if (newPassword !== confirmNewPassword) {
                showFeedback($errorElement, '새 비밀번호와 일치하지 않습니다.', false);
            } else {
                showFeedback($errorElement, '일치', true);
            }
        });

        // --- 공통: 사이드바 탭 전환 이벤트 ---
        $('.profile-sidebar a').on('click', function(e) {
            e.preventDefault();
            const targetId = $(this).attr('href');
            $('.profile-sidebar li, .tab-pane').removeClass('active');
            $(this).parent().addClass('active');
            $(targetId).addClass('active');
        });
    }


    // =======================================================================
    // 3. 기능별 함수 선언 (Function Declarations)
    // =======================================================================

    /**
     * [데이터 로딩] 현재 로그인된 사용자의 정보를 서버에서 불러와 각 폼에 채웁니다.
     */
    function loadMyInfo() {
        $.ajax({
            type: 'GET',
            url: '/api/v1/users/me',
            dataType: 'json'
        })
        .done(function(userInfo) {
            $('#userId').val(userInfo.userLgnId);
            $('#userName').val(userInfo.userNm);
            $('#userEmail').val(userInfo.emlAddr);
            $('#userTelNo').val(formatTelno(userInfo.telno));
            $('#userZipCode').val(userInfo.zipCd);
            $('#userAddress').val(userInfo.addr);
            $('#userDetailAddress').val(userInfo.daddr);
            $('#userBrdt').val(userInfo.userBrdt?.replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3'));
            $('#userNickname').val(userInfo.userNcnm);
            $('#userSelfIntro').val(userInfo.selfIntroCn);
            
            const profileImageElement = $('#profileImagePreview');
            if (userInfo.prflImg) {
                profileImageElement.attr('src', userInfo.prflImg).show();
            } else {
                profileImageElement.attr('src', '/uploads/profiles/defaultprofile.jpg').show();
            }
            
            validateEmail(userInfo.emlAddr);
            validateTelno(userInfo.telno);
        })
        .fail(function(xhr) {
            if (xhr.status === 401) {
                alert('로그인 후 이용해주세요.');
                window.location.href = '/customer/login'; 
            } else {
                alert('정보를 불러오는 중 오류가 발생했습니다.');
            }
        });
    }
    
    /**
     * [탭 1] '수정하기' 버튼 클릭 시, 회원정보(이름, 이메일, 주소 등)를 서버에 전송합니다.
     */
    function updateUserInfo(e) {
        e.preventDefault();
        
        if (!$('#userName').val()) {
            alert('이름은 필수 항목입니다.');
            $('#userName').focus();
            return;
        }
        if (!validateEmail($('#userEmail').val()) || !validateTelno($('#userTelNo').val())) {
            return;
        }
        
        const requestData = {
            userNm: $('#userName').val(),
            emlAddr: $('#userEmail').val(),
            telno: $('#userTelNo').val(),
            zipCd: $('#userZipCode').val(),
            addr: $('#userAddress').val(),
            daddr: $('#userDetailAddress').val()
        };

        $.ajax({
            type: 'PUT',
            url: '/api/v1/mypage/info',
            contentType: 'application/json',
            data: JSON.stringify(requestData)
        })
        .done(function() {
            alert('회원정보가 성공적으로 수정되었습니다.');
            loadMyInfo();
        })
        .fail(function(xhr) {
            $('#emailError, #telError').hide();
            const response = xhr.responseJSON;
            if (xhr.status === 409 && response && response.message) {
                if (response.message.includes('이메일')) $('#emailError').text(response.message).show();
                else if (response.message.includes('연락처')) $('#telError').text(response.message).show();
                else alert('오류: ' + response.message);
            } else {
                alert('오류: ' + (response?.message || '정보 수정에 실패했습니다.'));
            }
        });
    }

    /**
     * [탭 2] '저장하기' 버튼 클릭 시, 프로필 정보(닉네임, 자기소개, 이미지)를 서버에 전송합니다.
     */
	function updateProfileInfo(e) {
        e.preventDefault(); // 폼 기본 제출 동작 방지

	        const nickname = $('#userNickname').val();
	        const $nicknameErrorElement = $('#nicknameError');
	        const nicknameRegex = /^[a-zA-Z0-9가-힣]{2,15}$/;
	
	        // 1. 닉네임이 비어있는지 확인
	        if (nickname === '') {
	            $nicknameErrorElement.text('닉네임을 입력해주세요.').css('color', 'red').show();
	            return; // 유효성 검사 실패 시 함수 종료
	        }
	
	        // 2. 닉네임 정규식 유효성 검사
	        if (!nicknameRegex.test(nickname)) {
	            $nicknameErrorElement.text('닉네임은 2~15자리의 한글, 영문, 숫자만 사용 가능합니다.').css('color', 'red').show();
	            return; // 유효성 검사 실패 시 함수 종료
	        }
	
	        // 3. 닉네임 중복 확인 (AJAX 호출)
	        $.ajax({
	            type: 'GET',
	            url: '/api/v1/mypage/profile/check-nickname',
	            data: { 'userNcnm': nickname }
	        })
	        .done(function(response) {
	            if (response.isAvailable) { // 서버에서 닉네임이 '사용 가능'하다고 응답한 경우
	                $nicknameErrorElement.text('사용 가능한 닉네임입니다.').css('color', 'green').show();
	                // 닉네임 유효성 및 중복 확인 통과 후, 실제 프로필 저장 API 호출
	                submitProfileUpdateForm();
	            } else { // 서버에서 닉네임이 '이미 사용 중'이라고 응답한 경우
	                $nicknameErrorElement.text('이미 사용 중인 닉네임입니다.').css('color', 'red').show();
	            }
	        })
	        .fail(function(error) {
	            console.error("닉네임 중복 확인 중 에러 발생:", error);
	            $nicknameErrorElement.text('닉네임 확인 중 오류가 발생했습니다.').css('color', 'red').show();
	        });
	    }
		
		/**
	     * 닉네임 유효성 검사 통과 후 실제 프로필 업데이트를 수행하는 내부 함수
	     */
	    function submitProfileUpdateForm() {
	        const formData = new FormData();
	        formData.append('userNcnm', $('#userNickname').val());
	        formData.append('selfIntroCn', $('#userSelfIntro').val());
	        const imageFile = $('#profileImageInput')[0].files[0];
	        if (imageFile) {
	            formData.append('profileImageFile', imageFile);
	        }

	        $.ajax({
	            type: 'PUT',
	            url: '/api/v1/mypage/profile',
	            data: formData,
	            processData: false, // FormData 사용 시 필수
	            contentType: false  // FormData 사용 시 필수
	        })
	        .done(function(response) {
	            alert(response.message);
	            loadMyInfo(); // 정보 업데이트 후 다시 불러오기
	        })
	        .fail(function(xhr) {
	            alert('오류: ' + (xhr.responseJSON?.message || '프로필 저장에 실패했습니다.'));
	        });
	    }

    /**
     * [탭 3] '변경사항 저장' 버튼 클릭 시, 비밀번호 변경 정보를 서버에 전송합니다.
     */
    function changePassword(e) {
        e.preventDefault();

        $('.error-message').hide().text('');
        const currentPassword = $('#currentPassword').val();
        const newPassword = $('#newPassword').val();
        const confirmNewPassword = $('#confirmNewPassword').val();
        let isValid = true;

        if (!currentPassword) {
            $('#currentPasswordError').text('현재 비밀번호를 입력해주세요.').show();
            isValid = false;
        }
        if (!newPassword) {
            $('#newPasswordError').text('새 비밀번호를 입력해주세요.').show();
            isValid = false;
        } else if (!passwordRegex.test(newPassword)) {
            $('#newPasswordError').text('비밀번호는 8~20자이며, 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.').show();
            isValid = false;
        }
        if (!confirmNewPassword) {
            $('#confirmNewPasswordError').text('새 비밀번호 확인을 입력해주세요.').show();
            isValid = false;
        } else if (newPassword !== confirmNewPassword) {
            $('#confirmNewPasswordError').text('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.').show();
            isValid = false;
        }
        if (currentPassword && newPassword && currentPassword === newPassword) {
            $('#newPasswordError').text('현재 비밀번호와 다른 비밀번호를 입력해주세요.').show();
            isValid = false;
        }
        if (!isValid) return;

        const requestData = { currentPassword, newPassword, confirmNewPassword };

        $.ajax({
            type: 'PUT',
            url: '/api/v1/mypage/security/password',
            contentType: 'application/json',
            data: JSON.stringify(requestData)
        })
        .done(function(response) {
            alert(response.message);
            $('#password-change-form')[0].reset();
            $('#newPasswordError, #confirmNewPasswordError').hide().text(''); 
        })
        .fail(function(xhr) {
            const response = xhr.responseJSON;
            let errorMessage = response?.message || '비밀번호 변경 중 오류가 발생했습니다.';
            alert('오류: ' + errorMessage);
        });
    }


    // =======================================================================
    // 4. 유틸리티 함수 (Utility Functions)
    // =======================================================================

    /** [유틸] 전화번호 형식(010-1234-5678)으로 변환합니다. */
    function formatTelno(telno) {
        if (!telno) return '';
        const digitsOnly = telno.replace(/-/g, '');
        return (digitsOnly.length === 11) ? digitsOnly.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3') : telno;
    }

    /** [유틸] 다음 우편번호 API를 호출하여 주소를 검색합니다. */
    function searchZipCode() {
        new daum.Postcode({
            oncomplete: function(data) {
                $('#userZipCode').val(data.zonecode);
                $('#userAddress').val(data.roadAddress || data.jibunAddress);
                $('#userDetailAddress').focus();
            }
        }).open();
    }

    /** [유틸] 이메일 형식의 유효성을 검사하고 결과를 UI에 표시합니다. */
    function validateEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const $error = $('#emailError');
        if (!email) {
            $error.text("이메일을 입력해주세요.").show();
            return false;
        } else if (!regex.test(email)) {
            $error.text("올바른 이메일 형식이 아닙니다.").show();
            return false;
        }
        $error.hide();
        return true;
    }

    /** [유틸] 전화번호 형식의 유효성을 검사하고 결과를 UI에 표시합니다. */
    function validateTelno(telno) {
        const regex = /^010-?\d{3,4}-?\d{4}$/;
        const $error = $('#telError');
        if (!telno) {
            $error.text("휴대폰 번호를 입력해주세요.").show();
            return false;
        } else if (!regex.test(telno)) {
            $error.text("휴대폰 번호 형식(010-1234-5678)을 확인해주세요.").show();
            return false;
        }
        $error.hide();
        return true;
    }
    
    /** [유틸] 유효성 검사 피드백 메시지를 UI에 표시합니다. (성공: 초록색, 실패: 빨간색) */
    function showFeedback(element, message, isValid) {
        element.text(message).css('color', isValid ? 'green' : 'red');
        if (message) element.show(); else element.hide(); 
    }

    /** [유틸] URL의 쿼리 파라미터(?tab=...)를 읽어 해당 탭을 활성화합니다. */
    function activateTabFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        const tabId = urlParams.get('tab');
        if (tabId) {
            $('.profile-sidebar li, .tab-pane').removeClass('active');
            $(`.profile-sidebar a[href="#${tabId}"]`).parent().addClass('active');
            $(`#${tabId}`).addClass('active');
        } else {
            $('.profile-sidebar li:first-child, .tab-pane:first-child').addClass('active');
        }
    }


    // =======================================================================
    // 5. 스크립트 실행 (Execution)
    // =======================================================================
    
    // HTML 문서가 완전히 로드되면 initializePage 함수를 실행합니다.
    $(initializePage);

})(jQuery);
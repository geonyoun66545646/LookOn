/**
 * 
 */

$(document).ready(() => {

    // ===================================================================
    // 1. DOM 요소 캐싱 및 유효성 검사 관련 변수
    // ===================================================================

    // 자주 사용하는 jQuery 객체를 상수로 캐싱
    const $joinForm = $('#joinForm');
    const $idInput = $('#reg-id');
    const $idFeedback = $('#id-feedback');
    const $passwordInput = $('#reg-password');
    const $passwordFeedback = $('#password-feedback');
    const $passwordConfirmInput = $('#reg-password-confirm');
    const $passwordConfirmFeedback = $('#password-confirm-feedback');
    const $nameInput = $('#reg-name');
    const $nameFeedback = $('#name-feedback');
    const $nicknameInput = $('#reg-nickname');
    const $nicknameFeedback = $('#nickname-feedback');
    const $emailInput = $('#reg-email');
    const $emailFeedback = $('#email-feedback');
    const $tel1Input = $('#reg-tel1');
    const $tel2Input = $('#reg-tel2');
    const $tel3Input = $('#reg-tel3');
    const $telFeedback = $('#tel-feedback');
    const $zipcodeInput = $('#reg-zipcode');
    const $addrInput = $('#reg-addr');
    const $daddrInput = $('#reg-daddr');
    const $btnFindPostcode = $('#btn-find-postcode');
    const $pushAgreeCheckbox = $('#reg-push-agree');
    const $emailAgreeCheckbox = $('#reg-email-rcv-agree');
	const $genderFeedback = $('#gender-feedback');
	const $birthdateInput = $('#reg-birthdate');
	const $birthdateFeedback = $('#birthdate-feedback');
    
    // 유효성 검사 통과 여부 추적 객체
    const validationStatus = {
        id: false, 
		password: false, 
		passwordConfirm: false, 
		name: false,
		birthdate: false,
		gender: false,
        nickname: false, 
		email: false, 
		tel: false, 
		address: true // 주소는 선택사항으로 간주하거나, daddr 입력시 true로 변경
    };

    const showFeedback = (element, message, isValid) => {
        element.text(message).css('color', isValid ? 'green' : 'red');
    };

    const regex = {
        id: /^[a-zA-Z0-9]{5,20}$/,
        password: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()])[a-zA-Z\d!@#$%^&*()]{8,16}$/,
        name: /^[가-힣]{2,10}$/,
        nickname: /^[a-zA-Z0-9가-힣]{2,10}$/,
        email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
        tel: /^\d{3,4}$/
    };
    
    // ===================================================================
    // 2. 이벤트 핸들러 (캐싱된 변수 사용)
    // ===================================================================

	$idInput.on('blur', () => {
	        const id = $idInput.val();
	        if (!regex.id.test(id)) {
	            showFeedback($idFeedback, '아이디는 5~20자의 영문 또는 숫자여야 합니다.', false);
	            validationStatus.id = false;
	            return;
	        }
	        $.ajax({
	            url: '/api/customer/register/check-id', type: 'GET', data: { userLgnId: id },
	            success: isDuplicate => {
	                const isValid = !isDuplicate;
	                showFeedback($idFeedback, isValid ? '사용 가능한 아이디입니다.' : '이미 사용 중인 아이디입니다.', isValid);
	                validationStatus.id = isValid;
	            }
	        });
	    });
	// 엔터 키를 눌렀을 때도 blur 이벤트를 강제로 발생시켜 확인 로직 실행
    $idInput.on('keyup', e => {
        if (e.key === 'Enter') {
            $idInput.trigger('blur');
        }
    });
	$nicknameInput.on('blur', () => {
	       const nickname = $nicknameInput.val();
	       if (!regex.nickname.test(nickname)) {
	           showFeedback($nicknameFeedback, '닉네임은 2~10자의 영문, 숫자, 한글이어야 합니다.', false);
	           validationStatus.nickname = false;
	           return;
	       }
	       $.ajax({
	           url: '/api/customer/register/check-nickname', type: 'GET', data: { userNcnm: nickname },
	           success: isDuplicate => {
	               const isValid = !isDuplicate;
	               showFeedback($nicknameFeedback, isValid ? '사용 가능한 닉네임입니다.' : '이미 사용 중인 닉네임입니다.', isValid);
	               validationStatus.nickname = isValid;
	           }
	       });
	   });
   // 엔터 키를 눌렀을 때도 blur 이벤트를 강제로 발생시켜 확인 로직 실행
   $nicknameInput.on('keyup', e => {
       if (e.key === 'Enter') {
           $nicknameInput.trigger('blur');
       }
   });
   
   // 생년월일
   $birthdateInput.on('change', () => { 
       validationStatus.birthdate = !!$birthdateInput.val(); // 값이 있기만 하면 true
	   if (!validationStatus.birthdate) {
	               showFeedback($birthdateFeedback, '생년월일을 선택해주세요.', false);
	           } else {
	               $birthdateFeedback.text(''); // 값이 있으면 메시지 삭제
	           }
   });

   // 이메일
	$emailInput.on('blur', () => {
	    const email = $emailInput.val();
	    if (!regex.email.test(email)) {
	        showFeedback($emailFeedback, '유효하지 않은 이메일 형식입니다.', false);
	        validationStatus.email = false;
	        return;
	    }
	    $.ajax({
	        url: '/api/customer/register/check-email', type: 'GET', data: { emlAddr: email },
	        success: isDuplicate => {
	            const isValid = !isDuplicate;
	            showFeedback($emailFeedback, isValid ? '사용 가능' : '이미 사용 중', isValid);
	            validationStatus.email = isValid;
	        }
	    });
	});

    $passwordInput.on('keyup', () => {
        validationStatus.password = regex.password.test($passwordInput.val());
        showFeedback($passwordFeedback, validationStatus.password ? '안전' : '8~16자, 영문/숫자/특수문자 조합', validationStatus.password);
    });

    $passwordConfirmInput.on('keyup', () => {
        if ($passwordConfirmInput.val()) {
            validationStatus.passwordConfirm = ($passwordInput.val() === $passwordConfirmInput.val());
            showFeedback($passwordConfirmFeedback, validationStatus.passwordConfirm ? '일치' : '불일치', validationStatus.passwordConfirm);
        }
    });

    $nameInput.on('blur', () => {
        validationStatus.name = regex.name.test($nameInput.val());
        showFeedback($nameFeedback, validationStatus.name ? '' : '이름을 올바르게 입력해주세요.', validationStatus.name);
    });

	$tel1Input.add($tel2Input).add($tel3Input).on('blur', () => {
	    const tel1 = $tel1Input.val();
	    const tel2 = $tel2Input.val();
	    const tel3 = $tel3Input.val();

	    // 1. 세 칸이 모두 채워져 있는지 먼저 확인
	    if (!tel1 || !tel2 || !tel3) {
	        // 아직 모든 칸이 채워지지 않았으면, 섣불리 오류 메시지를 보여주지 않고 그냥 넘어감
	        // 사용자가 입력을 마쳤다고 판단하기 어려우므로.
	        // 단, validationStatus.tel은 false로 유지
	        validationStatus.tel = false;
	        return;
	    }

	    // 2. 모든 칸이 채워졌다면, 형식(길이, 숫자) 검사
	    const isTel1Valid = /^\d{3}$/.test(tel1);    // 정확히 3자리 숫자
	    const isTel2Valid = /^\d{3,4}$/.test(tel2); // 3자리 또는 4자리 숫자
	    const isTel3Valid = /^\d{4}$/.test(tel3);    // 정확히 4자리 숫자

	    const isFormatValid = isTel1Valid && isTel2Valid && isTel3Valid;

	    if (!isFormatValid) {
	        showFeedback($telFeedback, '전화번호 형식이 올바르지 않습니다.', false);
	        validationStatus.tel = false;
	        return; // 형식 오류 시 중단
	    }

	    // 3. 형식 검사를 통과하면, 중복 확인 AJAX 호출
	    const fullTelno = `${tel1}-${tel2}-${tel3}`;
	    $.ajax({
	        url: '/api/customer/register/check-telno',
	        type: 'GET',
	        data: { telno: fullTelno },
	        success: isDuplicate => {
	            const isValid = !isDuplicate;
	            const message = isValid ? '사용 가능한 전화번호입니다.' : '이미 등록된 전화번호입니다.';
	            showFeedback($telFeedback, message, isValid);
	            validationStatus.tel = isValid;
	        }
	    });
	});
	
	$('.btn-group-toggle > label.btn').on('click', () => {
	    // 클릭 이벤트가 발생하면, 잠시 후 라디오 버튼의 상태가 바뀌므로
	    // 약간의 지연을 주어 확실하게 처리
	    setTimeout(() => {
	        // 라디오 버튼 중 하나라도 체크되었는지 확인
	        if ($('input[name="genderSeCd"]:checked').length > 0) {
	            validationStatus.gender = true;
	            showFeedback($genderFeedback, '', true); // 피드백 메시지 초기화
	        }
	    }, 100); // 0.1초 지연
	});

    $daddrInput.on('blur', () => {
        validationStatus.address = $daddrInput.val().trim() !== '';
    });

    $btnFindPostcode.on('click', () => {
        new daum.Postcode({
            oncomplete: data => {
                $zipcodeInput.val(data.zonecode);
                $addrInput.val(data.roadAddress);
                $daddrInput.focus();
            }
        }).open();
    });

    $joinForm.on('submit', e => {
        e.preventDefault();
		
		console.log('최종 제출 직전 validationStatus 상태:', validationStatus); 
        
        for (const key in validationStatus) {
            if (!validationStatus[key]) {
                alert('모든 필수 항목을 올바르게 입력하고 중복 확인을 완료해주세요.');
                return;
            }
        }

        let jsonData = {};
        $joinForm.serializeArray().forEach(item => {
            jsonData[item.name] = item.value || "";
        });

        jsonData.pushNtfctnRcptnAgreYn = $pushAgreeCheckbox.is(':checked') ? 'true' : 'false';
        jsonData.emlRcptnAgreYn = $emailAgreeCheckbox.is(':checked') ? 'true' : 'false';

		console.log(JSON.stringify(jsonData));
		
        $.ajax({
            url: '/api/customer/register/join', 
			type: 'POST', 
			contentType: 'application/json',
            data: JSON.stringify(jsonData),
            success: response => {
                alert(response);
				$joinForm[0].reset();
                $('#signin-tab').tab('show');
                $joinForm[0].reset();
                $('.form-text').text('');
                Object.keys(validationStatus).forEach(key => validationStatus[key] = false);
            },
            error: xhr => {
                alert('회원가입 실패: ' + xhr.responseText);
            }
        });
    });
});
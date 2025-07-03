/**
 * 
 */

		$(document).ready(function() {
		
		    // ================== 공통 변수 선언 (오류 없는 최종 버전) ==================
		    const duplicatedIds = ['admin', 'test', 'root', 'ks55'];
		    const idInput = $('#register-id');
		    const idMessage = $('#id-check-message');
		    const detailInputs = $('.register-details-input');
		    const registerForm = $('#register-form');
		    const registerResultMessage = $('#register-result-message');
		    const registerSubmitButton = $('#btn-register-submit');
		    const findPostcodeButton = $('#btn-find-postcode');
		    
		 // ==========================================================
		//  1. 아이디 중복 확인 기능 (메시지 표시 문제 해결 버전)
		// ==========================================================
		    
		// 아이디 중복을 실제로 확인하는 함수
		function checkIdDuplication() {
		    const enteredId = idInput.val().trim();
		    if (enteredId.length === 0) {
		        idMessage.text('');
		        detailInputs.prop('disabled', true);
		        return;
		    }
		    if (duplicatedIds.includes(enteredId)) {
		        // [수정 포인트 1] 메시지를 표시하고,
		        idMessage.text('이미 사용 중인 아이디입니다.').css('color', 'red');
		        detailInputs.prop('disabled', true);
		        
		        // [수정 포인트 2] 포커스를 다시 주기 전에, 잠깐의 시간(1ms)을 줍니다.
		        // 이렇게 하면 focus 이벤트가 메시지를 바로 지우는 것을 방지할 수 있습니다.
		        setTimeout(function() {
		            idInput.focus();
		        }, 1);
		
		    } else {
		        idMessage.text('사용 가능한 아이디입니다.').css('color', 'green');
		        detailInputs.prop('disabled', false);
		        $('#register-password').focus();
		    }
		}
		
		// [이벤트] 아이디 입력창에서 포커스를 잃었을 때
		idInput.on('blur', checkIdDuplication);
		
		// [이벤트] 아이디 입력창에서 엔터 키를 눌렀을 때
		idInput.on('keyup', function(event) {
		    if (event.key === "Enter" || event.keyCode === 13) {
		        $(this).trigger('blur');
		    }
		});
		
		// [이벤트] 아이디 입력창에 다시 포커스를 맞췄을 때
		idInput.on('focus', function() {
		    // [수정 포인트 3] 메시지를 바로 지우는 대신, 사용자가 키보드를 입력 시작할 때 지우도록 변경
		    detailInputs.prop('disabled', true);
		    
		    // 사용자가 새로운 입력을 시작하면 메시지를 지우도록 keydown 이벤트를 한 번만 실행
		    idInput.one('keydown', function() {
		        idMessage.text('');
		    });
		});
		
		    // ==========================================================
		    //  2. 주소 찾기 API 기능
		    // ==========================================================
		    findPostcodeButton.on('click', function() {
		        new daum.Postcode({
		            oncomplete: function(data) {
		                let addr = (data.userSelectedType === 'R') ? data.roadAddress : data.jibunAddress;
		                $('#postcode').val(data.zonecode);
		                $('#address').val(addr);
		                $('#detailAddress').focus();
		            }
		        }).open();
		    });
		
		    // ==========================================================
		    //  3. 회원가입 버튼 클릭 이벤트
		    // ==========================================================
		    registerSubmitButton.on('click', function() {
		        if ($('#register-id').val().trim() === '' || $('#register-password').val().trim() === '' || $('#register-name').val().trim() === '' || $('#register-email').val().trim() === '') {
		            alert('필수 항목을 모두 입력해주세요.');
		            return;
		        }
		        
		        const formData = registerForm.serialize();
		        console.log('회원가입 폼 데이터:', formData);
		        
		        registerResultMessage.html('<p style="color:blue;">회원가입 처리 중...</p>');
		        
		        setTimeout(function() {
		            registerResultMessage.html('<p style="color:green;"><b>회원가입이 완료되었습니다!</b></p>');
		            
		            setTimeout(function() {
		                $('#signin-tab').tab('show');
		                registerForm[0].reset();
		                idMessage.text('');
		                registerResultMessage.text('');
		                detailInputs.prop('disabled', true);
		            }, 1500);
		
		        }, 1000);
		    });
		});
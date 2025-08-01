$(document).ready(function() {
    // --- 요소 캐싱 ---
    const $inquiryTargetSelect = $('#inqryTrgtTypeCd');
    const $storeSelectRow = $('#storeSelectRow');
    const $inquiryStoreSelect = $('#inqryStoreId');
    const $inquiryTypeSelect = $('#inqryTypeCd');
    const $form = $('#inquiryForm');
    const $fileInput = $('#attachedFilesInput');
    const $previewContainer = $('#filePreviewContainer');
    const $inquiryTitle = $('#inqryTtl');
    const $inquiryContent = $('#inqryCn');

    // --- Select2 초기화 ---
    $inquiryTargetSelect.select2({
        theme: 'bootstrap-5',
        width: '100%',
        dropdownParent: $inquiryTargetSelect.parent(),
        minimumResultsForSearch: Infinity
    });
    $inquiryTypeSelect.select2({
        theme: 'bootstrap-5',
        width: '100%',
        dropdownParent: $inquiryTypeSelect.parent(),
        minimumResultsForSearch: Infinity
    });
    $inquiryStoreSelect.select2({
        theme: 'bootstrap-5',
        width: '100%',
        dropdownParent: $inquiryStoreSelect.parent()
    });

    // --- 문의 유형 옵션 관리 ---
    const inquiryOptions = $inquiryTypeSelect.find("option").map(function() {
        if ($(this).val()) {
            return { value: $(this).val(), text: $(this).text() };
        }
    }).get();

    function populateInquiryTypeOptions(optionsData) {
        $inquiryTypeSelect.empty().append('<option></option>'); // placeholder를 위해 빈 option 유지
        optionsData.forEach(option => {
            $inquiryTypeSelect.append($('<option>', { value: option.value, text: option.text }));
        });
        $inquiryTypeSelect.trigger('change.select2'); // Select2 업데이트
    }

    // --- '문의 대상'에 따른 동적 UI 변경 로직 ---
    $inquiryTargetSelect.on('change', function() {
        const selectedTarget = $(this).val();
        if (selectedTarget === 'STORE') {
            $storeSelectRow.slideDown();
            $inquiryStoreSelect.prop('required', true); // 상점 선택 필수
        } else {
            $storeSelectRow.slideUp();
            $inquiryStoreSelect.val(null).trigger('change').prop('required', false); // 상점 선택 초기화 및 필수 해제
        }
    });

    // --- 유효성 검사 및 폼 제출 처리 ---
    $form.on('submit', function(event) {
        event.preventDefault(); // 기본 제출 동작 무조건 중지

        // 1. 기존 오류 상태 초기화
        $('.is-invalid').removeClass('is-invalid');
        
        let isFormValid = true;
        let firstInvalidElement = null;

        // 2. 유효성 검사 필드 정의
        const fieldsToValidate = [
            { element: $inquiryTargetSelect, name: '문의 대상' },
            { element: $inquiryTitle, name: '제목' },
            { element: $inquiryContent, name: '내용' },
            { element: $inquiryTypeSelect, name: '문의 유형' }
        ];

        // 상점이 선택 대상일 경우, 상점 필드도 검사 목록에 추가
        if ($inquiryTargetSelect.val() === 'STORE') {
            fieldsToValidate.splice(1, 0, { element: $inquiryStoreSelect, name: '상점' });
        }
        
        // 3. 각 필드 검사
        fieldsToValidate.forEach(function(field) {
            const $el = field.element;
            // required 속성이 있거나, 직접 검사 로직을 추가할 수 있음
            if ($el.prop('required') && (!$el.val() || $el.val().trim() === '')) {
                isFormValid = false;
                $el.addClass('is-invalid');
                
                // 첫 번째 오류 요소 저장
                if (!firstInvalidElement) {
                    firstInvalidElement = $el;
                }
            }
        });

		// 4. 최종 결정 및 처리
		        if (isFormValid) {
		            // 유효하면 fetch를 통해 폼 데이터 전송
		            const formData = new FormData(this);

		            // --- ✅ 이 부분이 수정됩니다 ---
		            fetch(this.action, {
		                method: this.method,
		                body: formData
		            })
		            .then(response => {
		                if (!response.ok) {
		                    // 서버에서 에러 응답(4xx, 5xx)을 보냈을 때
		                    return response.json().then(err => Promise.reject(err));
		                }
		                return response.json(); // 성공 응답
		            })
		            .then(data => {
		                if (data.status === 'success') {
		                    // 성공 시 SweetAlert
		                    Swal.fire({
		                        icon: 'success',
		                        title: '등록 완료',
		                        text: data.message || '문의가 성공적으로 등록되었습니다.',
		                        confirmButtonText: '확인'
		                    }).then((result) => {
		                        // 사용자가 '확인' 버튼을 누르면 목록 페이지로 이동
		                        if (result.isConfirmed) {
		                            window.location.href = '/customer/inquiry/inquiryList';
		                        }
		                    });
		                } else {
		                    // 서버에서 보낸 실패 메시지가 있을 때 SweetAlert
		                    Swal.fire({
		                        icon: 'error',
		                        title: '등록 실패',
		                        text: data.message || '알 수 없는 오류가 발생했습니다.',
		                        confirmButtonText: '확인'
		                    });
		                }
		            })
		            .catch(error => {
		                // 네트워크 오류 또는 JSON 파싱 실패 시 SweetAlert
		                Swal.fire({
		                    icon: 'error',
		                    title: '오류 발생',
		                    text: '요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
		                    footer: `<pre style="text-align: left; font-size: 0.8em;">${error.message || ''}</pre>`,
		                    confirmButtonText: '확인'
		                });
		                console.error('Fetch error:', error);
		            });
		            // --- ✅ 수정 끝 ---

        } else {
            // 유효하지 않으면 첫 번째 오류 필드로 스크롤 및 포커스
            if (firstInvalidElement) {
                // 화면 중앙으로 부드럽게 스크롤
                firstInvalidElement[0].scrollIntoView({
                    behavior: 'smooth',
                    block: 'center'
                });

                // Select2인 경우 드롭다운을 열고, 일반 입력창은 포커스
                if (firstInvalidElement.data('select2')) {
                    firstInvalidElement.select2('open');
                } else {
                    firstInvalidElement.focus();
                }
            }
        }
    });
    
    // --- 실시간 오류 제거 로직 ---
    // 사용자가 값을 입력/변경하면 'is-invalid' 클래스를 제거
    $('select.form-select').on('change', function() {
        if ($(this).val()) {
            $(this).removeClass('is-invalid');
        }
    });

    $('input.form-control, textarea.form-control').on('input', function() {
        if ($(this).val().trim() !== '') {
            $(this).removeClass('is-invalid');
        }
    });

	// --- 파일 미리보기 로직 수정 ---
	$fileInput.on('change', function() {
	        $previewContainer.empty();
	        const files = this.files;
	        if (files.length > 0) {
	            Array.from(files).forEach(file => {
	                const fileItem = $('<div class="file-preview-item"></div>'); 
	                
	                const fileNameWrapper = $('<div class="file-name-wrapper"></div>');
	                const fileNameSpan = $('<span class="file-name-text"></span>').text(file.name);
	                const MAX_FILE_SIZE = 5 * 1024 * 1024;

	                if (file.size > MAX_FILE_SIZE) {
	                    const sizeWarning = $('<span style="color: red;"></span>').text(
	                        ` (파일 크기 초과: ${Math.round(file.size / (1024 * 1024))}MB)`);
	                    fileNameSpan.append(sizeWarning);
	                }

	                if (file.type.startsWith('image/')) {
	                    const reader = new FileReader();
	                    reader.onload = function (e) {
	                        const imgPreview = $('<img>').attr('src', e.target.result).css({ maxWidth: '100px', maxHeight: '100px' });
	                        fileNameWrapper.prepend(imgPreview);
	                    };
	                    reader.readAsDataURL(file);
	                } else {
	                    const icon = $('<i class="fa fa-file-alt"></i>').css('fontSize', '24px');
	                    fileNameWrapper.prepend(icon);
	                }
	                
	                fileItem.append(fileNameWrapper);

	                // 🌟🌟🌟 이 부분이 'X' 아이콘을 추가하는 부분입니다 🌟🌟🌟
	                const deleteButton = $('<button type="button" class="file-remove-button"></button>');
	                // Font Awesome 'times' 아이콘을 버튼 내부에 추가
	                deleteButton.append('<i class="fas fa-times"></i>'); 

	                deleteButton.on('click', function() {
	                    $(this).closest('.file-preview-item').remove();
	                });

	                fileItem.append(deleteButton);
	                
	                $previewContainer.append(fileItem);
	            });
	        }
	    });
	});
// File: /js/customerjs/addInquiryView.js


	// --- 상점 선택 및 문의 유형 동적 처리 ---
$(document).ready(function () {
	
	// ✅ [수정] 각 Select2를 개별적으로 초기화하여 오류를 원천 차단합니다.

	    // 1. '문의 대상' 드롭다운 (검색창 없음)
	    $('#inqryTrgtTypeCd').select2({
	        theme: 'bootstrap-5',
	        width: '100%',
	        dropdownParent: $('#inqryTrgtTypeCd').parent(), // 자신의 부모를 명시
	        minimumResultsForSearch: Infinity // 검색창 숨기기
	    });

	    // 2. '문의 유형' 드롭다운 (검색창 없음)
	    $('#inqryTypeCd').select2({
	        theme: 'bootstrap-5',
	        width: '100%',
	        dropdownParent: $('#inqryTypeCd').parent(), // 자신의 부모를 명시
	        minimumResultsForSearch: Infinity // 검색창 숨기기
	    });

	    // 3. '상점 선택' 드롭다운 (검색창 있음)
	    $('#inqryStoreId').select2({
	        theme: 'bootstrap-5',
	        width: '100%',
	        dropdownParent: $('#inqryStoreId').parent() // 자신의 부모를 명시
	    });


	    // --- 여기부터 아래 코드는 그대로 유지 ---

	    // '문의 대상' 선택에 따른 '상점 선택' 드롭다운 동적 표시
	    $('#inqryTrgtTypeCd').on('change', function() {
	        const selectedTarget = $(this).val();
	        const $storeSelectRow = $('#storeSelectRow');

	        if (selectedTarget === 'STORE') {
	            $storeSelectRow.slideDown();
	        } else {
	            $storeSelectRow.slideUp();
	            $('#inqryStoreId').val(null).trigger('change');
	        }
	    });
	    
	    // (이하 기존 파일 업로드 관련 JS가 있다면 여기에 추가)
	    // 예시: 파일 드래그 앤 드롭 및 미리보기 로직
	    const fileDropZone = document.querySelector('.file-drop-zone');
	    const fileInput = document.getElementById('attachedFilesInput');
	    const filePreviewContainer = document.getElementById('filePreviewContainer');
	    const files = [];

	    fileDropZone.addEventListener('click', () => fileInput.click());
	    fileDropZone.addEventListener('dragover', (e) => e.preventDefault());
	    fileDropZone.addEventListener('drop', (e) => {
	        e.preventDefault();
	        handleFiles(e.dataTransfer.files);
	    });
	    fileInput.addEventListener('change', (e) => handleFiles(e.target.files));
	
	
	
    const $inquiryTargetSelect = $('#inqryTrgtTypeCd');
    const $storeSelectRow = $('#storeSelectRow');
    const $inquiryStoreSelect = $('#inqryStoreId');
    const $inquiryTypeSelect = $('#inqryTypeCd');
    const $form = $('#inquiryForm');

	const $inquiryOptions = $('#inqryTypeCd').find("option");
	const inquiryOptions = [];
	$inquiryOptions.each((idx, element)=> {
		 const value = $(element).val();
		 const text = $(element).text();
		 if(value) inquiryOptions.push({value, text})
	});
	
	
    function populateInquiryTypeOptions(optionsData) {
        $inquiryTypeSelect.empty().append('<option value="">선택하세요</option>');
        optionsData.forEach(option => {
            $inquiryTypeSelect.append($('<option>', { value: option.value, text: option.text }));
        });
    }

    function updateFormState(selectedTarget, selectedStoreId) {
        // 상점 선택 드롭다운 및 관련 row 처리
        if (selectedTarget === 'STORE') {
            $inquiryStoreSelect.prop('disabled', false).attr('required', 'required');
            $storeSelectRow.show();
        } else { // 'ADMIN'이거나 다른 값일 경우
            $inquiryStoreSelect.prop('disabled', true).val('').removeAttr('required');
        }

        // 문의 유형 드롭다운 처리
        if (selectedTarget === 'ADMIN') {
            populateInquiryTypeOptions(inquiryOptions);
            $inquiryTypeSelect.prop('disabled', false).attr('required', 'required');
        } else if (selectedTarget === 'STORE' && selectedStoreId !== '') {
            populateInquiryTypeOptions(inquiryOptions);
            $inquiryTypeSelect.prop('disabled', false).attr('required', 'required');
        } else {
            populateInquiryTypeOptions([]);
            $inquiryTypeSelect.prop('disabled', true).removeAttr('required');
        }
    }

    // 초기 상태 설정
    // 페이지 로드 시 현재 선택된 값에 따라 폼 상태 업데이트
    updateFormState($inquiryTargetSelect.val(), $inquiryStoreSelect.val());

    $inquiryTargetSelect.on('change', function () {
        updateFormState($(this).val(), $inquiryStoreSelect.val());
    });

    $inquiryStoreSelect.on('change', function () {
        if ($inquiryTargetSelect.val() === 'STORE') {
            updateFormState('STORE', $(this).val());
        }
    });

    // --- Form Submit ---
    $form.on('submit', function (event) {
        event.preventDefault();

        const formData = new FormData(this);

        if (!this.reportValidity()) {
            return;
        }

        fetch(this.action, {
            method: this.method,
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => {
                        throw new Error(err.message || `HTTP error! status: ${response.status}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.status === 'success') {
                    alert(data.message);
                    window.location.href = '/customer/inquiry/inquiryList';
                } else {
                    alert('문의 등록 실패: ' + (data.message || '오류발생'));
                }
            })
            .catch(error => {
                alert('오류! 잠시 후 다시 시도해주세요.');
                console.error('Fetch error:', error);
            });
    });

    // --- File Preview ---
    const $fileInput = $('#attachedFilesInput');
    const $previewContainer = $('#filePreviewContainer');

    $fileInput.on('change', function () {
        $previewContainer.empty();

        const files = this.files;
        if (files.length > 0) {
            Array.from(files).forEach(file => {
                const fileItem = $('<div class="file-preview-item d-flex align-items-center mt-2"></div>');

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
                        const img = $('<img>').attr('src', e.target.result).css({ maxWidth: '100px', maxHeight: '100px' });
                        fileNameWrapper.prepend(img);
                    };
                    reader.readAsDataURL(file);
                } else {
                    const icon = $('<i class="fa fa-file-alt"></i>').css('fontSize', '24px');
                    fileNameWrapper.prepend(icon);
                }

                fileNameWrapper.append(fileNameSpan);
                fileItem.append(fileNameWrapper);

                const deleteButton = $('<button type="button" class="btn btn-danger btn-extra-small">x</button>');
                deleteButton.on('click', () => fileItem.remove());

                const deleteWrapper = $('<div class="file-delete-button-container"></div>').append(deleteButton);
                fileItem.append(deleteWrapper);

                $previewContainer.append(fileItem);
            });
        }
    });
});
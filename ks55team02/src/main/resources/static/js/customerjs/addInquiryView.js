$(document).ready(function() {
    // --- 요소 캐싱 ---
    const $inquiryTargetSelect = $('#inqryTrgtTypeCd');
    const $storeSelectRow = $('#storeSelectRow');
    const $inquiryStoreSelect = $('#inqryStoreId');
    const $inquiryTypeSelect = $('#inqryTypeCd');
    const $form = $('#inquiryForm');
    const $fileInput = $('#attachedFilesInput');
    const $previewContainer = $('#filePreviewContainer');
    const $fileDropZone = $('.file-drop-zone');
    const $inquiryTitle = $('#inqryTtl');
    const $inquiryContent = $('#inqryCn');

    // 전송할 파일을 관리하기 위한 배열
    let managedFiles = [];

    // --- Select2 초기화 ---
    $inquiryTargetSelect.select2({
        theme: 'bootstrap-5',
        width: '100%',
        dropdownParent: $inquiryTargetSelect.parent(),
        placeholder: '문의 대상을 선택하세요',
        minimumResultsForSearch: Infinity
    });
    $inquiryTypeSelect.select2({
        theme: 'bootstrap-5',
        width: '100%',
        dropdownParent: $inquiryTypeSelect.parent(),
        placeholder: '문의 유형을 선택하세요',
        minimumResultsForSearch: Infinity
    });
    $inquiryStoreSelect.select2({
        theme: 'bootstrap-5',
        width: '100%',
        dropdownParent: $inquiryStoreSelect.parent(),
        placeholder: '문의할 상점을 검색하세요'
    });

    // --- '문의 대상'에 따른 동적 UI 변경 로직 ---
    $inquiryTargetSelect.on('change', function() {
        const selectedTarget = $(this).val();
        if (selectedTarget === 'STORE') {
            $storeSelectRow.slideDown();
            $inquiryStoreSelect.prop('required', true);
        } else {
            $storeSelectRow.slideUp();
            $inquiryStoreSelect.val(null).trigger('change').prop('required', false);
        }
    });

    // --- 파일 첨부 및 미리보기 로직 ---

    // 2. 드래그 앤 드롭 이벤트 처리
    $fileDropZone.on('dragover', (e) => {
        e.preventDefault();
        e.stopPropagation();
        $fileDropZone.addClass('dragging');
    });
    $fileDropZone.on('dragleave', (e) => {
        e.preventDefault();
        e.stopPropagation();
        $fileDropZone.removeClass('dragging');
    });
    $fileDropZone.on('drop', (e) => {
        e.preventDefault();
        e.stopPropagation();
        $fileDropZone.removeClass('dragging');
        const files = e.originalEvent.dataTransfer.files;
        addFiles(files); // 드롭된 파일을 관리 배열에 추가
    });

    // 3. 파일 입력(input)이 변경되었을 때 처리
    $fileInput.on('change', function() {
        const files = this.files;
        addFiles(files); // 선택된 파일을 관리 배열에 추가
        // 동일한 파일을 다시 선택할 수 있도록 input의 값을 초기화
        $(this).val('');
    });

    /**
     * @description 사용자가 선택한 파일들을 관리 배열(managedFiles)에 추가하는 함수
     * @param {FileList} files - 사용자가 선택한 파일 목록
     */
    function addFiles(files) {
        const MAX_FILES = 5; // 최대 파일 개수 제한
        if (managedFiles.length + files.length > MAX_FILES) {
            Swal.fire('파일 개수 초과', `첨부파일은 최대 ${MAX_FILES}개까지 등록할 수 있습니다.`, 'warning');
            return;
        }

        Array.from(files).forEach(file => {
            // 중복 파일 체크 (파일 이름과 크기가 모두 동일한 경우)
            if (!managedFiles.some(f => f.name === file.name && f.size === file.size)) {
                managedFiles.push(file);
            }
        });
        renderPreviews(); // 파일 목록이 변경되었으므로 미리보기를 다시 렌더링
    }

    /**
     * @description managedFiles 배열을 기반으로 파일 미리보기를 화면에 렌더링하는 함수
     */
	function renderPreviews() {
	    $previewContainer.empty(); // 미리보기 영역을 비움

	    // 드롭존 텍스트 또는 상태 업데이트
	    if (managedFiles.length > 0) {
	        $fileDropZone.addClass('has-files'); // 파일이 있으면 클래스 추가 (안내문구 숨김용)
	    } else {
	        $fileDropZone.removeClass('has-files'); // 파일이 없으면 클래스 제거
	    }

	    managedFiles.forEach((file, index) => {
	        // 각 파일을 감싸는 아이템. CSS에서 모양을 만듭니다.
	        const fileItem = $('<div class="file-preview-item"></div>');

	        // 파일 타입에 따라 이미지 또는 아이콘을 아이템에 직접 추가합니다.
	        if (file.type.startsWith('image/')) {
	            const reader = new FileReader();
	            reader.onload = function(e) {
	                // 이미지 태그를 생성하고 fileItem에 바로 추가
	                const imgPreview = $('<img>').attr('src', e.target.result);
	                fileItem.append(imgPreview);
	            };
	            reader.readAsDataURL(file);
	        } else {
	            // 아이콘 태그를 생성하고 fileItem에 바로 추가
	            const icon = $('<i class="fas fa-file-alt"></i>');
	            fileItem.append(icon);
	        }

	        // 삭제 버튼 추가 (배열의 인덱스를 data 속성으로 저장)
	        const deleteButton = $('<button type="button" class="file-remove-button"></button>')
	            .html('<i class="fas fa-times"></i>')
	            .attr('data-index', index);

	        fileItem.append(deleteButton);
	        
	        // 완성된 파일 아이템을 미리보기 컨테이너에 추가
	        $previewContainer.append(fileItem);
	    });
	}

    // 4. 미리보기 영역 내의 삭제 버튼 클릭 이벤트 처리 (이벤트 위임)
    $previewContainer.on('click', '.file-remove-button', function(event) {
		event.preventDefault();
		event.stopPropagation();
        const indexToRemove = $(this).data('index');
        managedFiles.splice(indexToRemove, 1); // data-index를 이용해 관리 배열에서 해당 파일 제거
        renderPreviews(); // 미리보기를 다시 렌더링
    });


    // --- 유효성 검사 및 폼 제출 처리 ---
    $form.on('submit', function(event) {
        event.preventDefault(); // 기본 제출 동작 무조건 중지

        // 1. 기존 오류 상태 초기화
        $('.is-invalid').removeClass('is-invalid');
        $form.find('.form-select').parent().find('.select2-selection').css('border-color', '');

        let isFormValid = true;
        let firstInvalidElement = null;

        // 2. 유효성 검사 필드 정의
        const fieldsToValidate = [
            { element: $inquiryTargetSelect, name: '문의 대상' },
            { element: $inquiryTitle, name: '제목' },
            { element: $inquiryContent, name: '내용' },
            { element: $inquiryTypeSelect, name: '문의 유형' }
        ];

        // '문의 대상'이 '상점'일 경우, 상점 선택 필드를 검사 목록에 추가
        if ($inquiryTargetSelect.val() === 'STORE') {
            fieldsToValidate.splice(1, 0, { element: $inquiryStoreSelect, name: '상점' });
        }

        // 3. 각 필드 검사
        fieldsToValidate.forEach(function(field) {
            const $el = field.element;
            if ($el.prop('required') && (!$el.val() || $el.val().trim() === '')) {
                isFormValid = false;
                if ($el.is('select')) {
                    $el.parent().find('.select2-selection').css('border-color', '#dc3545');
                } else {
                     $el.addClass('is-invalid');
                }

                if (!firstInvalidElement) {
                    firstInvalidElement = $el;
                }
            }
        });

        // 4. 최종 결정 및 처리
        if (isFormValid) {
            // 유효하면 FormData를 생성하고 파일을 추가
            const formData = new FormData(this);

            // FormData에서 기존 파일 필드를 삭제하고, 관리하던 파일들을 새로 추가
            formData.delete('attachedFiles');
            managedFiles.forEach(file => {
                formData.append('attachedFiles', file);
            });

            // fetch를 통해 폼 데이터 비동기 전송
            fetch(this.action, {
                method: this.method,
                body: formData
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => Promise.reject(err));
                }
                return response.json();
            })
            .then(data => {
                if (data.status === 'success') {
                    Swal.fire({
                        icon: 'success',
                        title: '등록 완료',
                        text: data.message || '문의가 성공적으로 등록되었습니다.',
                        confirmButtonText: '확인'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/customer/inquiry/inquiryList';
                        }
                    });
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: '등록 실패',
                        text: data.message || '알 수 없는 오류가 발생했습니다.',
                        confirmButtonText: '확인'
                    });
                }
            })
            .catch(error => {
                Swal.fire({
                    icon: 'error',
                    title: '오류 발생',
                    text: '요청 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
                    footer: `<pre style="text-align: left; font-size: 0.8em;">${error.message || ''}</pre>`,
                    confirmButtonText: '확인'
                });
                console.error('Fetch error:', error);
            });
        } else {
            // 유효하지 않으면 첫 번째 오류 필드로 스크롤 및 포커스
            if (firstInvalidElement) {
                firstInvalidElement[0].scrollIntoView({
                    behavior: 'smooth',
                    block: 'center'
                });

                if (firstInvalidElement.data('select2')) {
                    firstInvalidElement.select2('open');
                } else {
                    firstInvalidElement.focus();
                }
            }
        }
    });

    // --- 실시간 오류 제거 로직 ---
    // Select2의 오류 스타일 제거
    $('select.form-select').on('change', function() {
        if ($(this).val()) {
            $(this).parent().find('.select2-selection').css('border-color', '');
        }
    });
    // 일반 input, textarea의 오류 스타일 제거
    $('input.form-control, textarea.form-control').on('input', function() {
        if ($(this).val().trim() !== '') {
            $(this).removeClass('is-invalid');
        }
    });
});
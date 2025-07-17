$(() => {
    // --- 1. 요소 및 상태 변수 초기화 ---
    const feedForm = $('#feed-form');
    const imageInput = $('#image-input');
    const newPreviewContainer = $('#new-image-preview-container');
    const existingPreviewContainer = $('#existing-images-preview');
    const uploadPlaceholder = $('.upload-placeholder');
    const deleteImageSnsInput = $('#delete-image-sns');
    const cancelBtn = $('#cancel-btn');
    const submitBtn = $('#submit-btn');

    // [수정] 폼의 data-feed-sn 속성으로 작성/수정 모드 감지
    const feedSn = feedForm.data('feed-sn');
    const isEditMode = !!feedSn;

    // --- 2. 이벤트 핸들러 ---

    // [기능] '새 이미지 추가' input의 파일 선택이 변경되었을 때 (공통 로직)
    imageInput.on('change', function(event) {
        // 기존 새 이미지 미리보기 초기화
        newPreviewContainer.find('.preview-image-wrapper').remove(); 
        
        const files = event.target.files;
        if (files && files.length > 0) {
            uploadPlaceholder.hide();
            Array.from(files).forEach(file => {
                const reader = new FileReader();
                reader.onload = (e) => {
                    const imgWrapper = $('<div>').addClass('preview-image-wrapper');
                    const img = $('<img>').attr('src', e.target.result).addClass('preview-image');
                    imgWrapper.append(img);
                    newPreviewContainer.append(imgWrapper);
                };
                reader.readAsDataURL(file);
            });
        } else {
            // 이미지가 없고, 기존 이미지도 없는 경우에만 placeholder 표시
            if (existingPreviewContainer.find('.preview-image-wrapper:visible').length === 0) {
                uploadPlaceholder.show();
            }
        }
    });
    
    // [신규] '기존 이미지'의 'X' 삭제 버튼 클릭 시 (수정 모드 전용)
    if (isEditMode) {
        existingPreviewContainer.on('click', '.delete-existing-image-btn', function() {
            const imageContainer = $(this).closest('.preview-image-wrapper');
            const imageSnToDelete = imageContainer.data('image-sn');

            if (!imageSnToDelete) return;

            imageContainer.hide();

            const currentDeletedSns = deleteImageSnsInput.val();
            const newDeletedSns = currentDeletedSns ? `${currentDeletedSns},${imageSnToDelete}` : imageSnToDelete;
            deleteImageSnsInput.val(newDeletedSns);

            if (existingPreviewContainer.find('.preview-image-wrapper:visible').length === 0 && imageInput[0].files.length === 0) {
                uploadPlaceholder.show();
            }
        });
    }

    // [기능] '취소' 버튼 클릭 시 (공통 로직)
    cancelBtn.on('click', () => {
        if (confirm('작업을 취소하고 이전 페이지로 돌아가시겠습니까?')) {
            history.back();
        }
    });

    // [수정] 폼 제출(Submit) 처리 로직을 동적으로 변경
    feedForm.on('submit', function(event) {
        event.preventDefault(); 
        submitBtn.prop('disabled', true).text('처리 중...');

        // 1. FormData 구성
        const formData = new FormData();
        formData.append('feedCn', $('#feed-content').val());
        formData.append('hashtags', $('#feed-hashtags').val());

        // 2. 새 이미지 파일 추가
        const newImageFiles = imageInput[0].files;
        if (newImageFiles.length > 0) {
            for (let i = 0; i < newImageFiles.length; i++) {
                formData.append('newImageFiles', newImageFiles[i]);
            }
        }
        
        // 3. 수정 모드일 경우, 삭제할 이미지 정보 추가
        if (isEditMode) {
            formData.append('deleteImageSns', deleteImageSnsInput.val());
        }

        // 4. 유효성 검사
        const hasContent = !!formData.get('feedCn')?.trim();
        const hasNewImages = newImageFiles.length > 0;
        const hasExistingImages = isEditMode && existingPreviewContainer.find('.preview-image-wrapper:visible').length > 0;

        if (!hasContent && !hasNewImages && !hasExistingImages) {
            alert('내용이나 이미지를 하나 이상 추가해주세요.');
            submitBtn.prop('disabled', false).text(isEditMode ? '수정하기' : '작성하기');
            return;
        }

        // 5. Ajax 요청 URL 및 성공 메시지 설정
        const ajaxSettings = {
            url: isEditMode ? `/customer/api/feeds/edit/${feedSn}` : '/customer/api/feeds',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            successMessage: isEditMode ? '피드가 성공적으로 수정되었습니다.' : '피드가 성공적으로 작성되었습니다.',
            redirectUrl: isEditMode ? `/customer/feed/feedDetail/${feedSn}` : '/customer/feed/feedList'
        };

        // 6. Ajax 요청 실행
        $.ajax(ajaxSettings)
        .done(() => {
            alert(ajaxSettings.successMessage);
            window.location.href = ajaxSettings.redirectUrl;
        })
        .fail((jqXHR) => {
            if (jqXHR.status === 401) {
                alert('로그인이 필요합니다.');
                window.location.href = '/customer/login/login';
            } else {
                alert('처리 중 오류가 발생했습니다: ' + (jqXHR.responseJSON?.message || '서버 오류'));
            }
        })
        .always(() => {
            submitBtn.prop('disabled', false).text(isEditMode ? '수정하기' : '작성하기');
        });
    });

    // --- 초기 실행 ---
    // 수정 모드이고, 기존 이미지가 없으면 placeholder를 보여줌
    if (isEditMode && existingPreviewContainer.find('.preview-image-wrapper').length === 0) {
        uploadPlaceholder.show();
    } else if (!isEditMode) {
        uploadPlaceholder.show();
    } else {
        uploadPlaceholder.hide();
    }
});
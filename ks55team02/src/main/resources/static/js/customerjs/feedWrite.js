// 파일 경로: /js/customerjs/feedWrite.js
// 아래 코드로 파일 전체를 교체해주세요.

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

    const feedSn = feedForm.data('feed-sn');
    const isEditMode = !!feedSn;

    // --- 2. 이벤트 핸들러 ---

    // '새 이미지 추가' input 파일 선택 변경 시
    imageInput.on('change', function(event) {
        newPreviewContainer.find('.preview-image-wrapper').remove(); 
        
        const files = event.target.files;
        if (files && files.length > 0) {
            uploadPlaceholder.hide();
            const dataTransfer = new DataTransfer();
            Array.from(files).forEach(file => dataTransfer.items.add(file));
            imageInput[0].files = dataTransfer.files;

            Array.from(files).forEach((file, index) => {
                const reader = new FileReader();
                reader.onload = (e) => {
                    const imgWrapper = $('<div>').addClass('preview-image-wrapper').attr('data-file-index', index);
                    const img = $('<img>').attr('src', e.target.result).addClass('preview-image');
                    const deleteBtn = $('<button>').attr('type', 'button').addClass('delete-new-image-btn').html('×');
                    imgWrapper.append(img).append(deleteBtn);
                    newPreviewContainer.append(imgWrapper);
                };
                reader.readAsDataURL(file);
            });
        }
        if (newPreviewContainer.find('.preview-image-wrapper').length === 0) {
            if (existingPreviewContainer.find('.preview-image-wrapper:visible').length === 0) {
                uploadPlaceholder.show();
            }
        }
    });

    // 새로 추가한 이미지 미리보기의 'X' 삭제 버튼
    newPreviewContainer.on('click', '.delete-new-image-btn', function() {
        const wrapperToRemove = $(this).closest('.preview-image-wrapper');
        const fileIndexToRemove = parseInt(wrapperToRemove.data('file-index'), 10);
        
        const dataTransfer = new DataTransfer();
        const originalFiles = imageInput[0].files;
        
        for (let i = 0; i < originalFiles.length; i++) {
            if (i !== fileIndexToRemove) {
                dataTransfer.items.add(originalFiles[i]);
            }
        }
        imageInput[0].files = dataTransfer.files;

        wrapperToRemove.remove();

        newPreviewContainer.find('.preview-image-wrapper').each(function(newIndex) {
            $(this).attr('data-file-index', newIndex);
        });

        if (newPreviewContainer.find('.preview-image-wrapper').length === 0) {
           if (existingPreviewContainer.find('.preview-image-wrapper:visible').length === 0) {
                uploadPlaceholder.show();
            }
        }
    });
    
    // '기존 이미지'의 'X' 삭제 버튼 클릭 시 (수정 모드 전용)
    existingPreviewContainer.on('click', '.delete-existing-image-btn', function() {
        const imageWrapper = $(this).closest('.preview-image-wrapper');
        const imageSnToDelete = imageWrapper.data('image-sn');
        if (!imageSnToDelete) return;

        imageWrapper.hide();

        const currentDeletedSns = deleteImageSnsInput.val().split(',').filter(Boolean);
        if (!currentDeletedSns.includes(imageSnToDelete.toString())) {
            currentDeletedSns.push(imageSnToDelete);
            deleteImageSnsInput.val(currentDeletedSns.join(','));
        }

        if (existingPreviewContainer.find('.preview-image-wrapper:visible').length === 0 && imageInput[0].files.length === 0) {
            uploadPlaceholder.show();
        }
    });

    // '취소' 버튼 클릭 시
    cancelBtn.on('click', () => {
        if (confirm('작업을 취소하고 이전 페이지로 돌아가시겠습니까?')) {
            history.back();
        }
    });

    // 폼 제출(Submit) 처리 로직 통합
    feedForm.on('submit', function(event) {
        event.preventDefault(); 
        submitBtn.prop('disabled', true).text('처리 중...');

        const formData = new FormData(this);

        const hasContent = !!formData.get('feedCn')?.trim();
        const hasNewImages = imageInput[0].files.length > 0;
        const hasExistingImages = isEditMode && existingPreviewContainer.find('.preview-image-wrapper:visible').length > 0;

        if (!hasContent && !hasNewImages && !hasExistingImages) {
            alert('내용이나 이미지를 하나 이상 추가해주세요.');
            submitBtn.prop('disabled', false).text(isEditMode ? '수정하기' : '작성하기');
            return;
        }

        let hashtags = formData.get('hashtags');
        if (hashtags) {
            formData.set('hashtags', hashtags.replace(/#/g, ''));
        }

        const ajaxSettings = {
            url: isEditMode ? `/customer/api/feeds/edit/${feedSn}` : '/customer/api/feeds',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            successMessage: isEditMode ? '피드가 성공적으로 수정되었습니다.' : '피드가 성공적으로 작성되었습니다.',
            redirectUrl: isEditMode ? `/customer/feed/feedDetail/${feedSn}` : '/customer/feed/feedList'
        };

        $.ajax(ajaxSettings)
        .done(() => {
            alert(ajaxSettings.successMessage);
            window.location.href = ajaxSettings.redirectUrl;
        })
        .fail((jqXHR) => {
            // [핵심 수정] 401 에러 시, 강제 페이지 이동 대신 로그인 모달을 띄웁니다.
            if (jqXHR.status === 401) {
                $('#signin-modal').modal('show'); // 로그인 모달 띄우기
            } else if (jqXHR.status === 403) {
                alert('수정할 권한이 없습니다.');
            } else {
                alert('처리 중 오류가 발생했습니다: ' + (jqXHR.responseJSON?.message || '서버 오류'));
            }
        })
        .always(() => {
            submitBtn.prop('disabled', false).text(isEditMode ? '수정하기' : '작성하기');
        });
    });

    // --- 초기 실행 ---
    if (!isEditMode) {
        uploadPlaceholder.show();
    } else {
        if (existingPreviewContainer.find('.preview-image-wrapper').length === 0) {
            uploadPlaceholder.show();
        } else {
            uploadPlaceholder.hide();
        }
        const hashtagInput = $('#feed-hashtags');
        let tags = hashtagInput.val().trim();
        if (tags && !tags.startsWith('#')) {
            tags = '#' + tags.replace(/\s+/g, ' #');
            hashtagInput.val(tags);
        }
    }
});
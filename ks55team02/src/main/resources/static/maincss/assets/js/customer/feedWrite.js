$(() => {
    const imageInput = $('#image-input');
    const previewContainer = $('#image-preview-container');
    const uploadPlaceholder = $('.upload-placeholder');
    const feedForm = $('#feed-form');
    
    // --- 이미지 미리보기 기능 ---
    imageInput.on('change', function(event) {
        const files = event.target.files;
        previewContainer.find('.preview-image-wrapper').remove(); 
        
        if (files && files.length > 0) {
            uploadPlaceholder.hide();
            Array.from(files).forEach(file => {
                const reader = new FileReader();
                reader.onload = (e) => {
                    const imgWrapper = $('<div>').addClass('preview-image-wrapper');
                    const img = $('<img>').attr('src', e.target.result).addClass('preview-image');
                    imgWrapper.append(img);
                    previewContainer.append(imgWrapper);
                };
                reader.readAsDataURL(file);
            });
        } else {
            uploadPlaceholder.show();
        }
    });

    // [핵심 수정] 중복되던 클릭 이벤트 핸들러를 완전히 제거합니다.
    // previewContainer.on('click', ...); <-- 이 부분 전체 삭제

    // --- 폼 제출(Submit) 처리 ---
    feedForm.on('submit', function(event) {
        event.preventDefault(); 
        const submitBtn = $('#submit-btn');
        submitBtn.prop('disabled', true).text('업로드 중...');
        const formData = new FormData();
        const feedContent = $('#feed-content').val();
        formData.append('feedCn', feedContent);
        const imageFiles = imageInput[0].files;
        if (imageFiles.length > 0) {
            for (let i = 0; i < imageFiles.length; i++) {
                formData.append('imageFiles', imageFiles[i]);
            }
        }
        if (!feedContent.trim() && imageFiles.length === 0) {
            alert('내용이나 이미지를 하나 이상 추가해주세요.');
            submitBtn.prop('disabled', false).text('작성하기');
            return; 
        }
        $.ajax({
            url: '/customer/feed', type: 'POST', data: formData,
            processData: false, contentType: false
        }).done(() => {
            alert('피드가 성공적으로 작성되었습니다.');
            window.location.href = '/customer/feed/feedList';
        }).fail((jqXHR) => {
            if (jqXHR.status === 401) {
                alert('로그인이 필요합니다.');
                window.location.href = '/customer/login/login';
            } else {
                alert('피드 작성 중 오류가 발생했습니다.');
            }
        }).always(() => {
            submitBtn.prop('disabled', false).text('작성하기');
        });
    });
});
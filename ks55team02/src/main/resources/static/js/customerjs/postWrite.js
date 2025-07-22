/**
 * postWrite.js v3.0 (파일 업로드 버전)
 * - FormData 객체를 사용하여 텍스트와 파일 동시 전송
 * - 이미지 미리보기 기능 추가
 * - 기존 이미지 삭제 처리 기능 추가
 */
$(() => {
    const $postForm = $('#postForm');
    const $imagePreviewContainer = $('#image-preview-container');
    const $imageFileInput = $('#newImageFiles');
    
    // 삭제할 기존 이미지의 sn을 담을 배열
    let deleteImageSns = [];

    // --- 이벤트 핸들러 ---
    
    // 폼 제출 이벤트
    $postForm.on('submit', e => {
        e.preventDefault();
        
        // FormData 객체 생성
        const formData = new FormData(e.target);
        
        // 삭제하기로 한 기존 이미지 sn 목록을 formData에 추가
        deleteImageSns.forEach(sn => {
            formData.append('deleteImageSns', sn);
        });

        const pstSn = formData.get('pstSn');
        const isUpdateMode = pstSn && pstSn.trim() !== '';
        
        // 로딩 인디케이터 등 UI 처리 (필요 시 추가)
        const $submitButton = $postForm.find('button[type="submit"]');
        $submitButton.prop('disabled', true).text('처리 중...');

        $.ajax({
            url: isUpdateMode ? `/customer/api/post/update/${pstSn}` : '/customer/api/post/write',
            type: 'POST',
            data: formData,
            processData: false, // FormData 사용 시 필수
            contentType: false, // FormData 사용 시 필수
            dataType: 'json',
            success: response => {
                if (response.result === 'success') {
                    alert(response.message);
                    location.href = '/customer/post/postList';
                } else {
                    alert(response.message || '처리 중 오류가 발생했습니다.');
                    $submitButton.prop('disabled', false).text(isUpdateMode ? '수정 완료' : '작성 완료');
                }
            },
            error: (jqXHR) => {
                console.error('AJAX Error:', jqXHR);
                const errorResponse = jqXHR.responseJSON;
                alert(errorResponse ? errorResponse.message : '요청 처리 중 심각한 오류가 발생했습니다.');
                $submitButton.prop('disabled', false).text(isUpdateMode ? '수정 완료' : '작성 완료');
            }
        });
    });

    // 새 이미지 파일 선택 시 미리보기 생성
    $imageFileInput.on('change', e => {
        const files = e.target.files;
        if (!files) return;

        // 기존에 추가했던 '새 이미지' 미리보기는 모두 제거
        $imagePreviewContainer.find('.new-image').remove();

        Array.from(files).forEach(file => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const previewHtml = `
                    <div class="image-preview-item new-image">
                        <img src="${e.target.result}" alt="${file.name}">
                    </div>
                `;
                $imagePreviewContainer.append(previewHtml);
            };
            reader.readAsDataURL(file);
        });
    });

    // 기존 이미지의 삭제 버튼 클릭 이벤트
    $imagePreviewContainer.on('click', '.existing-image-delete-btn', function() {
        const $item = $(this).closest('.image-preview-item');
        const imageSn = $item.data('image-sn');
        
        if (imageSn) {
            // 삭제 목록에 추가하고 화면에서 숨김
            if (!deleteImageSns.includes(imageSn)) {
                 deleteImageSns.push(imageSn);
            }
            $item.fadeOut(300, function() { $(this).remove(); });
            console.log('삭제할 이미지 목록:', deleteImageSns);
        }
    });
});
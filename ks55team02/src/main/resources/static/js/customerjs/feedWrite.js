

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

	// '새 이미지 추가' input 파일 선택 변경 시 (단일 파일 처리 로직으로 변경)
	imageInput.on('change', function(event) {
	    const file = event.target.files[0];

	    // 새로운 이미지가 선택되지 않았으면 아무 작업도 하지 않음
	    if (!file) {
	        // 만약 사용자가 파일 선택을 취소했고, 보이는 이미지가 없다면 placeholder를 다시 표시
	        if (newPreviewContainer.find('.preview-image-wrapper').length === 0 && existingPreviewContainer.find('.preview-image-wrapper:visible').length === 0) {
	            uploadPlaceholder.show();
	        }
	        return;
	    }

	    // --- 단일 이미지 규칙 적용 ---
	    // 1. 기존에 있던 '새 이미지' 미리보기를 모두 제거
	    newPreviewContainer.find('.preview-image-wrapper').remove();

	    // 2. (수정 모드) 기존 이미지가 있다면 모두 숨기고 '삭제 목록'에 추가
	    if (isEditMode) {
	        existingPreviewContainer.find('.preview-image-wrapper:visible').each(function() {
	            const imageWrapper = $(this);
	            const imageSnToDelete = imageWrapper.data('image-sn');
	            if (!imageSnToDelete) return;

	            imageWrapper.hide(); // 화면에서 숨김

	            const currentDeletedSns = deleteImageSnsInput.val().split(',').filter(Boolean);
	            if (!currentDeletedSns.includes(imageSnToDelete.toString())) {
	                currentDeletedSns.push(imageSnToDelete);
	                deleteImageSnsInput.val(currentDeletedSns.join(','));
	            }
	        });
	    }
	    
	    // 3. 업로드 placeholder 숨기기
	    uploadPlaceholder.hide();

	    // 4. 선택된 단일 파일의 미리보기 생성
	    const reader = new FileReader();
	    reader.onload = (e) => {
	        const imgWrapper = $('<div>').addClass('preview-image-wrapper');
	        const img = $('<img>').attr('src', e.target.result).addClass('preview-image');
	        const deleteBtn = $('<button>').attr('type', 'button').addClass('delete-new-image-btn').html('×');
	        imgWrapper.append(img).append(deleteBtn);
	        newPreviewContainer.append(imgWrapper);
	    };
	    reader.readAsDataURL(file);
	});

	// 새로 추가한 이미지 미리보기의 'X' 삭제 버튼 (단일 파일 처리 로직으로 변경)
	newPreviewContainer.on('click', '.delete-new-image-btn', function() {
	    // 미리보기 제거
	    $(this).closest('.preview-image-wrapper').remove();
	    
	    // input의 파일 값 초기화
	    imageInput.val('');

	    // 보이는 이미지가 아무것도 없으면 placeholder 다시 표시
	    if (existingPreviewContainer.find('.preview-image-wrapper:visible').length === 0) {
	        uploadPlaceholder.show();
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
				// 401 에러 시, 강제 페이지 이동 대신 로그인 모달을 띄웁니다.
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
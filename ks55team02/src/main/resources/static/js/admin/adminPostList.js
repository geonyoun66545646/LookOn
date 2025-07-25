$(() => {
	const mainContent = $('main.main-content');

	const searchAndReloadList = (currentPage = 1) => {
		const searchCriteria = {
			filterConditions: $('input[name="filterConditions"]:checked').map((i, el) => $(el).val()).get(),
			startDate: $('input[name="startDate"]').val(),
			endDate: $('input[name="endDate"]').val(),
			searchKey: $('select[name="searchKey"]').val(),
			searchValue: $('input[name="searchValue"]').val(),
			sortOrder: $('#sortOrder').val(),
			psize: $('#pageSize').val(),
			currentPage: currentPage
		};

		$.ajax({
			url: '/adminpage/boardadmin/postManagement/postSearch',
			type: 'GET',
			data: $.param(searchCriteria, true),
			success: fragment => {
				$('#postListContainer').replaceWith(fragment);
			},
			// 에러 메시지
			error: () => alert("게시글 목록을 불러오는 데 실패했습니다.")
		});
	};

	// --- 이벤트 핸들러 (피드와 동일한 로직) ---

	mainContent.on('click', '#searchBtn', () => searchAndReloadList(1));
	mainContent.on('keyup', 'input[name="searchValue"]', e => {
		if (e.key === 'Enter') $('#searchBtn').click();
	});
	mainContent.on('click', '#filterCheckAll', e => {
		$('.filter-check').prop('checked', $(e.currentTarget).is(':checked'));
	});
	mainContent.on('change', '#sortOrder, #pageSize', () => searchAndReloadList(1));

	const processPostStatusChange = (postSns, action) => {
		const actionText = action === 'hide' ? '숨김 처리' : '복구';
		if (confirm(`${postSns.length}개의 게시글을 '${actionText}'하시겠습니까?`)) {
			$.ajax({
				url: '/adminpage/boardadmin/postManagement/updateStatus',
				type: 'POST',
				contentType: 'application/json; charset=utf-8',
				data: JSON.stringify({ postSns: postSns, action: action }),
				dataType: 'json',
				success: response => {
					alert(response.message);
					if (response.result === 'success') {
						const currentPage = $('.pagination .page-item.active .page-link').data('page') || 1;
						searchAndReloadList(currentPage);
					}
				},
				error: xhr => alert(xhr.responseJSON?.message || '서버와 통신 중 오류가 발생했습니다.')
			});
		}
	};

	// 일괄 처리
	mainContent.on('click', '.change-status-btn', e => {
		e.preventDefault();
		const selectedPostSns = $('.post-check:checked').map((i, el) => $(el).val()).get();
		if (selectedPostSns.length === 0) {
			return alert('처리할 게시글을 먼저 선택해주세요.');
		}
		const action = $(e.currentTarget).data('action');
		processPostStatusChange(selectedPostSns, action);
	});

	// 개별 처리
	mainContent.on('click', '.individual-action-btn', e => {
		const $btn = $(e.currentTarget);
		const postSn = $btn.data('post-sn');
		const action = $btn.data('action');
		processPostStatusChange([postSn], action);
	});

	// 전체 선택/해제 로직
	mainContent.on('click', '#checkAll', e => {
		$('.post-check').prop('checked', $(e.currentTarget).is(':checked'));
	});
	mainContent.on('click', '.post-check', () => {
		const totalChecks = $('.post-check').length;
		const checkedChecks = $('.post-check:checked').length;
		$('#checkAll').prop('checked', totalChecks > 0 && totalChecks === checkedChecks);
	});

	// 페이지네이션 클릭
	mainContent.on('click', '.pagination .page-link', e => {
		e.preventDefault();
		searchAndReloadList($(e.currentTarget).data('page'));
	});
	
	// --- 미리보기 모달 로직 추가 (adminPostList.js 버전) ---
	const previewModal = new bootstrap.Modal($('#previewModal')[0]);
	const $modalBody = $('#previewModalBody');

	// 피드 미리보기 HTML 생성 함수 (비워둠)
	const createFeedPreviewHtml = (data) => { return ''; };

	// 게시글 미리보기 HTML 생성 함수
	const createPostPreviewHtml = (data) => {
	    // 이미지 캐러셀 HTML
	    const imagesHtml = data.imageUrls && data.imageUrls.length > 0 ? `
	        <div id="previewCarousel" class="carousel slide mb-3" data-bs-ride="carousel">
	             <div class="carousel-inner">
	                ${data.imageUrls.map((url, index) => `
	                    <div class="carousel-item ${index === 0 ? 'active' : ''}">
	                        <img src="${url}" class="d-block w-100" alt="이미지 ${index + 1}" style="max-height: 400px; object-fit: contain;">
	                    </div>
	                `).join('')}
	            </div>
	            ${data.imageUrls.length > 1 ? `
	                <button class="carousel-control-prev" type="button" data-bs-target="#previewCarousel" data-bs-slide="prev"><span class="carousel-control-prev-icon"></span></button>
	                <button class="carousel-control-next" type="button" data-bs-target="#previewCarousel" data-bs-slide="next"><span class="carousel-control-next-icon"></span></button>
	            ` : ''}
	        </div>` : '';

	    const formattedDate = new Date(data.createdDate).toLocaleString('ko-KR');

	    return `
	        <div class="mb-3">
	            <h3>${data.postTitle}</h3>
	            <div class="text-muted">
	                <span><span class="badge bg-info">${data.boardName}</span></span>
	                <span class="mx-2">|</span>
	                <span>작성자: ${data.userNickname}</span>
	                <span class="mx-2">|</span>
	                <span>작성일: ${formattedDate}</span>
	            </div>
	        </div>
	        <hr>
	        ${imagesHtml}
	        <div class="mt-3" style="white-space: pre-wrap; min-height: 150px;">${data.postContent || '내용 없음'}</div>
	        <hr>
	        <div>
	            <span class="me-3"><i class="fas fa-eye"></i> 조회수 ${data.viewCount}</span>
	            <span class="me-3"><i class="fas fa-heart text-danger"></i> 좋아요 ${data.likeCount}</span>
	            <span><i class="fas fa-comment"></i> 댓글 ${data.commentCount}</span>
	        </div>
	    `;
	};

	// 미리보기 버튼 클릭 이벤트 위임 (피드와 동일한 코드)
	mainContent.on('click', '.preview-btn', e => {
	    const $btn = $(e.currentTarget);
	    const type = $btn.data('type');
	    const id = $btn.data('id');
	    const url = type === 'feed' ?
	        `/adminpage/boardadmin/feedManagement/preview/${id}` :
	        `/adminpage/boardadmin/postManagement/preview/${id}`;
	    
	    $modalBody.html('<div class="text-center"><div class="spinner-border" role="status"></div></div>');
	    previewModal.show();

	    $.ajax({
	        url: url,
	        type: 'GET'
	    }).done(data => {
	        const html = type === 'feed' ? createFeedPreviewHtml(data) : createPostPreviewHtml(data);
	        $modalBody.html(html);
	    }).fail(() => {
	        $modalBody.html('<p class="text-center text-danger">미리보기를 불러오는 데 실패했습니다.</p>');
	    });
	});
});
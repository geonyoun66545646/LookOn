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
			// Controller의 GetMapping 경로와 정확히 일치
			url: '/adminpage/boardadmin/feedManagement/feedSearch',
			type: 'GET',
			data: $.param(searchCriteria, true),
			success: fragment => {
				$('#feedListContainer').replaceWith(fragment);
			},
			error: () => alert("피드 목록을 불러오는 데 실패했습니다.")
		});
	};

	// --- 이벤트 핸들러 ---

	mainContent.on('click', '#searchBtn', () => searchAndReloadList(1));
	mainContent.on('keyup', 'input[name="searchValue"]', e => {
		if (e.key === 'Enter') $('#searchBtn').click();
	});
	mainContent.on('click', '#filterCheckAll', e => {
		$('.filter-check').prop('checked', $(e.currentTarget).is(':checked'));
	});
	mainContent.on('change', '#sortOrder, #pageSize', () => searchAndReloadList(1));

	const processFeedStatusChange = (feedSns, action) => {
		const actionText = action === 'hide' ? '숨김 처리' : '복구';
		if (confirm(`${feedSns.length}개의 피드를 '${actionText}'하시겠습니까?`)) {
			$.ajax({
				//Controller의 PostMapping 경로와 정확히 일치
				url: '/adminpage/boardadmin/feedManagement/updateStatus',
				type: 'POST',
				contentType: 'application/json; charset=utf-8',
				data: JSON.stringify({ feedSns: feedSns, action: action }),
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
		const selectedFeedSns = $('.feed-check:checked').map((i, el) => $(el).val()).get();
		if (selectedFeedSns.length === 0) {
			return alert('처리할 피드를 먼저 선택해주세요.');
		}
		const action = $(e.currentTarget).data('action');
		processFeedStatusChange(selectedFeedSns, action);
	});

	// 개별 처리
	mainContent.on('click', '.individual-action-btn', e => {
		const $btn = $(e.currentTarget);
		const feedSn = $btn.data('feed-sn');
		const action = $btn.data('action');
		processFeedStatusChange([feedSn], action); // feedSn 하나를 배열에 담아 함수 재사용
	});

	mainContent.on('click', '#checkAll', e => {
		$('.feed-check').prop('checked', $(e.currentTarget).is(':checked'));
	});
	mainContent.on('click', '.feed-check', () => {
		const totalChecks = $('.feed-check').length;
		const checkedChecks = $('.feed-check:checked').length;
		$('#checkAll').prop('checked', totalChecks > 0 && totalChecks === checkedChecks);
	});
	mainContent.on('click', '.pagination .page-link', e => {
		e.preventDefault();
		searchAndReloadList($(e.currentTarget).data('page'));
	});
	
	// --- 미리보기 모달 로직 추가 ---
	const previewModal = new bootstrap.Modal($('#previewModal')[0]);
	const $modalBody = $('#previewModalBody');

	// 수정 후 (이미지 태그를 완전히 제거한 최종 버전)
	const createFeedPreviewHtml = (data) => {
	    // 이미지 캐러셀 HTML 생성
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
	                <button class="carousel-control-prev" type="button" data-bs-target="#previewCarousel" data-bs-slide="prev">
	                    <span class="carousel-control-prev-icon" aria-hidden="true"></span><span class="visually-hidden">Previous</span>
	                </button>
	                <button class="carousel-control-next" type="button" data-bs-target="#previewCarousel" data-bs-slide="next">
	                    <span class="carousel-control-next-icon" aria-hidden="true"></span><span class="visually-hidden">Next</span>
	                </button>
	            ` : ''}
	        </div>` : '<p class="text-muted">이미지가 없습니다.</p>';

	    // 해시태그 HTML 생성
	    const tagsHtml = data.tags && data.tags.length > 0 ?
	        data.tags.map(tag => `<span class="badge bg-secondary me-1">#${tag}</span>`).join(' ') :
	        '<span class="text-muted">태그 없음</span>';

	    // 날짜 포맷팅
	    const formattedDate = new Date(data.crtDt).toLocaleString('ko-KR');

	    return `
	        <div class="mb-3">
	            <div class="fw-bold">${data.userNcnm}</div>
	            <div class="text-muted small">${formattedDate}</div>
	        </div>
	        <hr>
	        ${imagesHtml}
	        <p style="white-space: pre-wrap;">${data.feedCn || '내용 없음'}</p>
	        <div class="mb-2">
	            ${tagsHtml}
	        </div>
	        <hr>
	        <div>
	            <span class="me-3"><i class="fas fa-heart text-danger"></i> 좋아요 ${data.likeCount}</span>
	            <span><i class="fas fa-comment"></i> 댓글 ${data.commentCount}</span>
	        </div>
	    `;
	};

	// 게시글 미리보기 HTML 생성 함수 (일단 adminFeedList.js에는 비워둠)
	const createPostPreviewHtml = (data) => { return ''; };

	// 미리보기 버튼 클릭 이벤트 위임
	mainContent.on('click', '.preview-btn', e => {
	    const $btn = $(e.currentTarget);
	    const type = $btn.data('type');
	    const id = $btn.data('id');
	    const url = type === 'feed' ?
	        `/adminpage/boardadmin/feedManagement/preview/${id}` :
	        `/adminpage/boardadmin/postManagement/preview/${id}`;
	    
	    // 모달 내용 초기화
	    $modalBody.html('<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>');
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
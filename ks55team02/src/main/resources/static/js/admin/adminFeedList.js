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
			psize: $('#pageSize').val(), // [수정] 키를 'pageSize'에서 'psize'로 변경
			currentPage: currentPage
		};

		$.ajax({
			url: '/adminpage/boardadmin/feedmanagement/feedSearch',
			type: 'GET',
			data: $.param(searchCriteria, true),
			success: fragment => {
				$('#feedListContainer').replaceWith(fragment);
			},
			error: () => alert("피드 목록을 불러오는 데 실패했습니다.")
		});
	};

	// --- (이하 모든 이벤트 핸들러 로직은 이전과 동일합니다) ---
	mainContent.on('click', '#searchBtn', () => searchAndReloadList(1));
	mainContent.on('keyup', 'input[name="searchValue"]', e => {
		if (e.key === 'Enter') $('#searchBtn').click();
	});
	mainContent.on('click', '#filterCheckAll', e => {
		$('.filter-check').prop('checked', $(e.currentTarget).is(':checked'));
	});
	mainContent.on('change', '#sortOrder, #pageSize', () => searchAndReloadList(1));
	mainContent.on('click', '.change-status-btn', e => {
		e.preventDefault();
		const selectedFeedSns = $('.feed-check:checked').map((i, el) => $(el).val()).get();
		if (selectedFeedSns.length === 0) {
			return alert('처리할 피드를 먼저 선택해주세요.');
		}
		const action = $(e.currentTarget).data('action');
		const actionText = action === 'hide' ? '숨김 처리' : '숨김 해제';
		if (confirm(`${selectedFeedSns.length}개의 피드를 '${actionText}'하시겠습니까?`)) {
			$.ajax({
				url: '/adminpage/boardadmin/feedmanagement/updateStatus',
				type: 'POST',
				contentType: 'application/json; charset=utf-8',
				data: JSON.stringify({ feedSns: selectedFeedSns, action: action }),
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
	});

	// [신규] 개별 처리 버튼 클릭
	mainContent.on('click', '.individual-action-btn', e => {
		const $btn = $(e.currentTarget);
		const feedSn = $btn.data('feed-sn');
		const action = $btn.data('action');
		const actionText = action === 'hide' ? '숨김 처리' : '복구';

		if (confirm(`해당 피드를 '${actionText}'하시겠습니까?`)) {
			// 기존 일괄 처리 API를 재사용하되, feedSn 하나만 배열에 담아 전송
			$.ajax({
				url: '/adminpage/boardadmin/feedmanagement/updateStatus',
				type: 'POST',
				contentType: 'application/json; charset=utf-8',
				data: JSON.stringify({ feedSns: [feedSn], action: action }), // feedSn을 배열로 감싸서 전송
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
});
$(() => {
	const boardModal = new bootstrap.Modal($('#board-modal')[0]);
	const $modalTitle = $('#modal-title');
	const $boardForm = $('#board-form');
	const $boardMode = $('#board-mode');
	const $bbsClsfCd = $('#bbsClsfCd');

	// 새 게시판 생성 버튼 클릭
	$('#add-board-btn').on('click', () => {
		$boardMode.val('add');
		$modalTitle.text('새 게시판 생성');
		$boardForm[0].reset();
		$bbsClsfCd.prop('readonly', false);
		boardModal.show();
	});

	// 수정 버튼 클릭
	$('#board-table-body').on('click', '.edit-btn', function() {
		const boardCode = $(this).data('board-code');

		$.ajax({
			url: `/adminpage/boardadmin/boardManagement/boards/${boardCode}`,
			type: 'GET'
		}).done(board => {
			$boardMode.val('edit');
			$modalTitle.text('게시판 수정');
			$bbsClsfCd.val(board.bbsClsfCd).prop('readonly', true);
			$('#bbsNm').val(board.bbsNm);
			$('#bbsPrpsCn').val(board.bbsPrpsCn);
			$('#wrtAuthrtLvlVal').val(board.wrtAuthrtLvlVal);
			$('#cmntWrtAuthrtLvlVal').val(board.cmntWrtAuthrtLvlVal);
			boardModal.show();
		}).fail(() => alert('게시판 정보를 불러오는 데 실패했습니다.'));
	});

	// 모달 저장 버튼 클릭
	$('#save-board-btn').on('click', () => {
		const mode = $boardMode.val();
		const boardData = {
			bbsClsfCd: $bbsClsfCd.val(),
			bbsNm: $('#bbsNm').val(),
			bbsPrpsCn: $('#bbsPrpsCn').val(),
			wrtAuthrtLvlVal: $('#wrtAuthrtLvlVal').val(),
			cmntWrtAuthrtLvlVal: $('#cmntWrtAuthrtLvlVal').val()
		};

		// 유효성 검사
		if (!boardData.bbsClsfCd || !boardData.bbsNm) {
			alert('게시판 코드와 이름은 필수 항목입니다.');
			return;
		}

		const isAddMode = mode === 'add';
		const url = isAddMode ? '/adminpage/boardadmin/boardManagement/boards' : '/adminpage/boardadmin/boardManagement/boards';
		const method = isAddMode ? 'POST' : 'PUT';

		$.ajax({
			url: url,
			type: method,
			contentType: 'application/json',
			data: JSON.stringify(boardData)
		}).done(() => {
			alert(`게시판이 성공적으로 ${isAddMode ? '생성' : '수정'}되었습니다.`);
			boardModal.hide();
			location.reload(); // 성공 시 페이지 새로고침
		}).fail(res => alert(res.responseText || '처리 중 오류가 발생했습니다.'));
	});

	// 삭제 버튼 클릭
	$('#board-table-body').on('click', '.delete-btn', function() {
		const boardCode = $(this).data('board-code');
		if (confirm(`'${boardCode}' 게시판을 정말로 삭제하시겠습니까?`)) {
			$.ajax({
				url: `/adminpage/boardadmin/boardManagement/boards/${boardCode}`,
				type: 'DELETE'
			}).done(() => {
				alert('게시판이 삭제되었습니다.');
				location.reload();
			}).fail(res => alert(res.responseText || '삭제 중 오류가 발생했습니다.'));
		}
	});

	// [신규] 복구 버튼 클릭
	$('#board-table-body').on('click', '.restore-btn', function() {
		const boardCode = $(this).data('board-code');
		if (confirm(`'${boardCode}' 게시판을 정말로 복구하시겠습니까?`)) {
			$.ajax({
				url: `/adminpage/boardadmin/boardManagement/boards/${boardCode}/restore`,
				type: 'PUT' // PUT 메소드 사용
			}).done(() => {
				alert('게시판이 복구되었습니다.');
				location.reload();
			}).fail(res => alert(res.responseText || '복구 중 오류가 발생했습니다.'));
		}
	});
});

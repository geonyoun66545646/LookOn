$(() => {
    const mainContent = $('main.main-content'); 

    const searchAndReloadList = (currentPage = 1) => {
        const searchCriteria = {
            filterConditions: $('input[name="filterConditions"]:checked').map((i, el) => $(el).val()).get(),
            startDate:        $('input[name="startDate"]').val(),
            endDate:          $('input[name="endDate"]').val(),
            searchKey:        $('select[name="searchKey"]').val(),
            searchValue:      $('input[name="searchValue"]').val(),
            sortOrder:        $('#sortOrder').val(),
            pageSize:         $('#pageSize').val(),
            currentPage:      currentPage
        };
        
        $.ajax({
            url: '/adminpage/boardadmin/feedmanagement/feedSearch', // URL 수정
            type: 'GET',
            data: $.param(searchCriteria, true),
            success: fragment => {
                $('#feedListContainer').replaceWith(fragment);
            },
            error: () => alert("피드 목록을 불러오는 데 실패했습니다.")
        });
    };
    
    // --- 이벤트 핸들러 ---
    
    // 검색 버튼 클릭
    mainContent.on('click', '#searchBtn', () => searchAndReloadList(1));
    
    // Enter 키로 검색
    mainContent.on('keyup', 'input[name="searchValue"]', e => {
        if (e.key === 'Enter') $('#searchBtn').click();
    });

    // '분류' 전체 선택/해제
    mainContent.on('click', '#filterCheckAll', e => {
        $('.filter-check').prop('checked', $(e.currentTarget).is(':checked'));
    });
    
    // 정렬, N개씩 보기 변경
    mainContent.on('change', '#sortOrder, #pageSize', () => searchAndReloadList(1));

    // 선택한 피드 일괄 처리 (숨김/복구)
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
                url: '/adminpage/boardadmin/feedmanagement/updateStatus', // API URL 수정
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

    // 테이블 헤더 체크박스로 전체 선택/해제
    mainContent.on('click', '#checkAll', e => {
        $('.feed-check').prop('checked', $(e.currentTarget).is(':checked'));
    });

    // 개별 체크박스 선택 시, 헤더 체크박스 상태 동기화
    mainContent.on('click', '.feed-check', () => {
        const totalChecks = $('.feed-check').length;
        const checkedChecks = $('.feed-check:checked').length;
        $('#checkAll').prop('checked', totalChecks > 0 && totalChecks === checkedChecks);
    });

    // 페이지네이션 링크 클릭
    mainContent.on('click', '.pagination .page-link', e => {
        e.preventDefault();
        searchAndReloadList($(e.currentTarget).data('page'));
    });
});
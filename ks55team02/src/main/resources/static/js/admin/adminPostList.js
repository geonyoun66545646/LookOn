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
            psize:            $('#pageSize').val(),
            currentPage:      currentPage
        };
        
        $.ajax({
            // [수정] Controller의 GetMapping 경로와 정확히 일치
            url: '/adminpage/boardadmin/postManagement/postSearch',
            type: 'GET',
            data: $.param(searchCriteria, true),
            // [수정] 성공 시 교체될 컨테이너 ID
            success: fragment => {
                $('#postListContainer').replaceWith(fragment);
            },
            // [수정] 에러 메시지
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
                // [수정] Controller의 PostMapping 경로와 정확히 일치
                url: '/adminpage/boardadmin/postManagement/updateStatus',
                type: 'POST',
                contentType: 'application/json; charset=utf-8',
                // [수정] 서버로 보낼 데이터의 키를 'postSns'로 변경
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
        // [수정] 체크박스 클래스명 변경
        const selectedPostSns = $('.post-check:checked').map((i, el) => $(el).val()).get();
        if (selectedPostSns.length === 0) {
            // [수정] 알림 메시지
            return alert('처리할 게시글을 먼저 선택해주세요.');
        }
        const action = $(e.currentTarget).data('action');
        processPostStatusChange(selectedPostSns, action);
    });

    // 개별 처리
    mainContent.on('click', '.individual-action-btn', e => {
        const $btn = $(e.currentTarget);
        // [수정] data 속성명 변경
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
});
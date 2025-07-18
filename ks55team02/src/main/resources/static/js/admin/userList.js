/**
 * 
 */

$(() => {
	        // ===== 1. 공통 변수 =====
	        const mainContent = $('main.main-content'); 
	
	        // ===== 2. 핵심 함수: 목록 조회 및 새로고침 =====
	        const searchAndReloadList = (currentPage = 1) => {
	            const searchCriteria = {
	                statusList:  $('input[name="statusList"]:checked').map((i, el) => $(el).val()).get(),
	                startDate:   $('input[name="startDate"]').val(),
	                endDate:     $('input[name="endDate"]').val(),
	                searchKey:   $('select[name="searchKey"]').val(),
	                searchValue: $('input[name="searchValue"]').val(),
	                sortOrder:   $('#sortOrder').val(),
	                pageSize:    $('#pageSize').val(),
	                currentPage: currentPage
	            };
	            
	            $.ajax({
	                url: '/adminpage/useradmin/userstatussearch',
	                type: 'GET',
	                data: $.param(searchCriteria, true),
	                success: fragment => {
	                    // 목록 영역을 서버에서 받은 새 HTML로 교체
	                    $('#userListContainer').replaceWith(fragment);
	
	                    // (페이지 갱신 후 검색 조건 유지)
	                    $('input[name="startDate"]').val(searchCriteria.startDate);
	                    $('input[name="endDate"]').val(searchCriteria.endDate);
	                    $('select[name="searchKey"]').val(searchCriteria.searchKey);
	                    $('input[name="searchValue"]').val(searchCriteria.searchValue);
	                    $('.status-check').prop('checked', false);
	                    searchCriteria.statusList?.forEach(status => {
	                        $(`.status-check[value="${status}"]`).prop('checked', true);
	                    });
	                },
	                error: () => alert("회원 목록을 불러오는 데 실패했습니다.")
	            });
	        };
	        
	        // ===== 3. 이벤트 핸들러 모음 =====
	        
	        // 검색 버튼 클릭
	        mainContent.on('click', '#searchBtn', () => searchAndReloadList(1));
	        
	        // '분류' 전체 선택/해제
	        mainContent.on('click', '#filterCheckAll', e => {
	            $('.status-check').prop('checked', $(e.currentTarget).is(':checked'));
	        });
	        
	        // 정렬, N개씩 보기 변경
	        mainContent.on('change', '#sortOrder, #pageSize', () => searchAndReloadList(1));
	
	        // 선택한 회원 상태 일괄 변경
	        mainContent.on('click', '.change-status-btn', e => {
	            e.preventDefault();
	            
	            const selectedUserNos = $('.member-check:checked').map((i, el) => $(el).val()).get();
	            if (selectedUserNos.length === 0) {
	                return alert('상태를 변경할 회원을 먼저 선택해주세요.');
	            }
	
	            const targetStatus = $(e.currentTarget).data('status');
	            
	            if (confirm(`${selectedUserNos.length}명의 회원을 '${targetStatus}' 상태로 변경하시겠습니까?`)) {
	                $.ajax({
	                    url: '/adminpage/useradmin/updateUserStatus',
	                    type: 'POST',
	                    contentType: 'application/json; charset=utf-8',
	                    data: JSON.stringify({ userNos: selectedUserNos, status: targetStatus }),
	                    dataType: 'json',
	                    success: response => {
	                        alert(response.message);
	                        if (response.result === 'success') {
	                            const currentPage = $('.pagination .page-item.active .page-link').data('page') || 1;
	                            searchAndReloadList(currentPage);
	                        }
	                    },
	                    error: xhr => {
	                        const errorMsg = xhr.responseJSON?.message || '서버와 통신 중 오류가 발생했습니다.';
	                        alert(errorMsg);
	                    }
	                });
	            }
	        });
	
	        // 테이블 헤더 체크박스로 전체 선택/해제
	        mainContent.on('click', '#checkAll', e => {
	            $('.member-check').prop('checked', $(e.currentTarget).is(':checked'));
	        });
	
	        // 개별 체크박스 선택 시, 헤더 체크박스 상태 동기화
	        mainContent.on('click', '.member-check', () => {
	            const totalChecks = $('.member-check').length;
	            const checkedChecks = $('.member-check:checked').length;
	            $('#checkAll').prop('checked', totalChecks > 0 && totalChecks === checkedChecks);
	        });
	
	        // 페이지네이션 링크 클릭
	        mainContent.on('click', '.pagination .page-link', e => {
	            e.preventDefault();
	            searchAndReloadList($(e.currentTarget).data('page'));
	        });
	    });
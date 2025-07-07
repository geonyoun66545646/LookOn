/**
 * 
 */

$(() => {
	        // ===== 1. 공통 변수 =====
	        const mainContent = $('main.main-content'); 
	
	        // ===== 2. 핵심 함수: 목록 조회 및 새로고침 =====
	        const searchAndReloadList = (currentPage = 1) => {
	            const searchCriteria = {
	                startDate:   $('input[name="startDate"]').val(),
	                endDate:     $('input[name="endDate"]').val(),
	                searchKey:   $('select[name="searchKey"]').val(),
	                searchValue: $('input[name="searchValue"]').val(),
	                sortOrder:   $('#sortOrder').val(),
	                pageSize:    $('#pageSize').val(),
	                currentPage: currentPage
	            };
	            
	            $.ajax({
	                url: '/adminpage/useradmin/loginHistory/search',
	                type: 'GET',
	                data: $.param(searchCriteria, true), // 배열 값 처리를 위해 $.param 사용
	                success: fragment => {
	                    // 목록 영역을 서버에서 받은 새 HTML로 교체
	                    $('#userListContainer').replaceWith(fragment);
	
	                    // (페이지 갱신 후 검색 조건 유지)
	                    $('input[name="startDate"]').val(searchCriteria.startDate);
	                    $('input[name="endDate"]').val(searchCriteria.endDate);
	                    $('input[name="searchValue"]').val(searchCriteria.searchValue);
	                },
	                error: () => alert("로그인 기록을 불러오는 데 실패했습니다.")
	            });
	        };
	        
	        // ===== 3. 이벤트 핸들러 모음 =====
	        
	        // 검색 버튼 클릭
	        mainContent.on('click', '#searchBtn', () => searchAndReloadList(1));
	        
	        // 검색어 입력 후 엔터
	        mainContent.on('keypress', 'input[name="searchValue"]', e => {
	            if (e.which === 13) { $('#searchBtn').click(); }
	        });
	        
	        // 정렬, N개씩 보기 변경
	        mainContent.on('change', '#sortOrder, #pageSize', () => searchAndReloadList(1));
	        
	        // 페이지네이션 링크 클릭
	        mainContent.on('click', '.pagination .page-link', e => {
	            e.preventDefault();
	            searchAndReloadList($(e.currentTarget).data('page'));
	        });
	    });
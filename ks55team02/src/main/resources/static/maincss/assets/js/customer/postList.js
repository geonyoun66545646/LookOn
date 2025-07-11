$(document).ready(function() {

    // --- 1. 상태 관리 및 초기화 ---
    const initialState = {
        bbsClsfCd: $('[data-initial-bbsClsfCd]').data('initial-bbsClsfCd') || null,
        page: $('[data-initial-page]').data('initial-page') || 1,
        size: $('[data-initial-size]').data('initial-size') || 10,
        keyword: $('[data-initial-keyword]').data('initial-keyword') || null
    };
    
    // 이 코드를 위해 postList.html의 메인 컨테이너에 data-* 속성 추가가 필요합니다.
    // 예: <div class="container" th:data-initial-bbsClsfCd="${bbsClsfCd}" ... >

    // --- 2. 이벤트 핸들러 바인딩 ---

    // 게시판 탭(카테고리) 클릭 이벤트
    $('.board-tabs-container').on('click', '.nav-link', function(e) {
        e.preventDefault();
        const url = new URL($(this).attr('href'), window.location.origin);
        initialState.bbsClsfCd = url.searchParams.get('bbsClsfCd');
        initialState.page = 1; // 카테고리 변경 시 1페이지로 리셋
        fetchAndRenderPosts(initialState);
    });

    // 페이지네이션 링크 클릭 이벤트
    $('#pagination-container').on('click', '.page-link', function(e) {
        e.preventDefault();
        const url = new URL($(this).attr('href'), window.location.origin);
        initialState.page = url.searchParams.get('page');
        fetchAndRenderPosts(initialState);
    });

    // 검색 폼 제출 이벤트
    $('#tempSearchAndSizeForm').on('submit', function(e) {
        e.preventDefault();
        initialState.keyword = $(this).find('input[name="keyword"]').val();
        initialState.page = 1; // 검색 시 1페이지로 리셋
        fetchAndRenderPosts(initialState);
    });

    // 페이지 크기 변경 이벤트
    $('#tempPageSizeSelect').on('change', function() {
        initialState.size = $(this).val();
        initialState.page = 1; // 크기 변경 시 1페이지로 리셋
        fetchAndRenderPosts(initialState);
    });


    // --- 3. 핵심 함수: 데이터 요청 및 렌더링 ---
    function fetchAndRenderPosts(params) {
        $.ajax({
            url: '/customer/post/list-data',
            type: 'GET',
            data: params,
            dataType: 'json',
            success: function(response) {
                renderTable(response.postList, params);
                renderPagination(response);
                // 필요 시 제목 업데이트: $('#board-title').text(response.boardName);
            },
            error: function(err) {
                console.error("데이터를 불러오는 데 실패했습니다.", err);
                $('#post-list-body').html('<tr><td colspan="6">게시글을 불러오는 중 오류가 발생했습니다.</td></tr>');
            }
        });
    }

    // --- 4. 렌더링 헬퍼 함수 ---

    // 테이블 본문 렌더링 함수
    function renderTable(postList, params) {
        const $tbody = $('#post-list-body');
        $tbody.empty(); // 기존 내용 비우기

        if (!postList || postList.length === 0) {
            $tbody.html('<tr><td colspan="6">게시글이 존재하지 않습니다.</td></tr>');
            return;
        }

        postList.forEach(function(p) {
            const commentCount = p.cmntCnt > 0 ? `<span>[<span>${p.cmntCnt}</span>]</span>` : '';
            const postLink = `/customer/post/postView?pstSn=${p.pstSn}&bbsClsfCd=${params.bbsClsfCd || ''}&page=${params.page}&size=${params.size}`;
            
            const rowHtml = `
                <tr>
                    <td>${p.pstSn}</td>
                    <td class="post-title-cell">
                        <a href="${postLink}" class="post-title-link">${p.pstTtl}</a>
                        ${commentCount}
                    </td>
                    <td>${p.userInfo ? p.userInfo.userNcnm : '알 수 없음'}</td>
                    <td>${p.interCnt}</td>
                    <td>${p.viewCnt}</td>
                    <td>${formatDate(p.crtDt)}</td>
                </tr>
            `;
            $tbody.append(rowHtml);
        });
    }

    // 페이지네이션 렌더링 함수
    function renderPagination(data) {
        const $paginationContainer = $('#pagination-container');
        $paginationContainer.empty();
        
        if (!data.totalPage || data.totalPage <= 1) return;

        let paginationHtml = '<nav aria-label="Page navigation"><ul class="pagination justify-content-center">';

        // '이전' 버튼
        if (data.currentPage > 1) {
            paginationHtml += `<li class="page-item"><a class="page-link" href="/customer/post/postList?page=${data.currentPage - 1}&size=${data.size}&bbsClsfCd=${data.bbsClsfCd || ''}">이전</a></li>`;
        }

        // 페이지 번호
        for (let i = data.startPageNum; i <= data.endPageNum; i++) {
            const activeClass = (i === data.currentPage) ? 'active' : '';
            paginationHtml += `<li class="page-item ${activeClass}"><a class="page-link" href="/customer/post/postList?page=${i}&size=${data.size}&bbsClsfCd=${data.bbsClsfCd || ''}">${i}</a></li>`;
        }

        // '다음' 버튼
        if (data.currentPage < data.totalPage) {
            paginationHtml += `<li class="page-item"><a class="page-link" href="/customer/post/postList?page=${data.currentPage + 1}&size=${data.size}&bbsClsfCd=${data.bbsClsfCd || ''}">다음</a></li>`;
        }
        
        paginationHtml += '</ul></nav>';
        $paginationContainer.html(paginationHtml);
    }

    // 날짜 포맷팅 함수 (기존 Thymeleaf 로직과 유사하게 구현)
    function formatDate(dateString) {
        const date = new Date(dateString);
        const today = new Date();
        
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');

        if (date.getFullYear() === today.getFullYear() &&
            date.getMonth() === today.getMonth() &&
            date.getDate() === today.getDate()) {
            return `${hours}:${minutes}`; // 오늘 날짜면 HH:mm
        } else {
            return `${month}-${day}`; // 다른 날짜면 MM-dd
        }
    }
    
    // --- 5. 최초 데이터 로드 ---
    // 이 JS파일 상단에 있던 initialState를 사용하기 위해 아래와 같이 수정
    // (이 코드가 제대로 동작하려면 postList.html 수정이 필요합니다.)
    const $container = $('.container'); // postList.html의 메인 컨테이너
    const initialParams = {
        bbsClsfCd: $container.data('initial-bbsclsfcd'),
        page: $container.data('initial-page'),
        size: $container.data('initial-size'),
        keyword: $container.data('initial-keyword')
    };
    fetchAndRenderPosts(initialParams);

});
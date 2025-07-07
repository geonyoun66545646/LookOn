/**
 * postList.js
 * 게시글 목록 페이지의 동적 기능 (페이지네이션, 검색, 카테고리 등) 처리
 */

$(() => {

    // --- 상수 선언 ---
    const $searchInput = $('input[placeholder="검색어를 입력하세요"]');
    const $pageSizeSelect = $('#pageSizeSelectForm');
    const $tabsContainer = $('.board-tabs-container');
    const bbsClsfCd = '';
    const size = 10;
    const searchKeyword = '';

    // --- 검색 기능 초기화 ---
    if($searchInput.length) {
        $searchInput.val(searchKeyword);
        $searchInput.on('keypress', e => {
            if(e.key === 'Enter') {
                e.preventDefault();
                const searchTerm = $searchInput.val().trim();
                const currentSize = $pageSizeSelect.length ? $pageSizeSelect.val() : size;
                window.location.href = `/customer/post/postList?bbsClsfCd=${bbsClsfCd}&page=1&size=${currentSize}&searchKeyword=${searchTerm}`;
            }
        });
    }

    // --- 페이지 사이즈 변경 기능 ---
    if($pageSizeSelect.length) {
        $pageSizeSelect.on('change', e => {
            const searchTerm = $searchInput.length ? $searchInput.val().trim() : searchKeyword;
            window.location.href = `/customer/post/postList?bbsClsfCd=${bbsClsfCd}&page=1&size=${$(e.target).val()}&searchKeyword=${searchTerm}`;
        });
    }

    // --- 탭 드래그 스크롤 기능 ---
    if($tabsContainer.length) {
        let isDown = false, startX, scrollLeft;

        $tabsContainer.on('mousedown', e => {
            isDown = true;
            $tabsContainer.addClass('active');
            startX = e.pageX - $tabsContainer.offset().left;
            scrollLeft = $tabsContainer.scrollLeft();
        }).on('mouseleave mouseup', () => {
            isDown = false;
            $tabsContainer.removeClass('active');
        }).on('mousemove', e => {
            if(!isDown) return;
            e.preventDefault();
            const x = e.pageX - $tabsContainer.offset().left;
            const walk = (x - startX) * 2.5;
            $tabsContainer.scrollLeft(scrollLeft - walk);
        });
    }
});
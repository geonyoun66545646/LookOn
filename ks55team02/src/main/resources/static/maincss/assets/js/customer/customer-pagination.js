/**
 * 
 */

/**
 * 페이지네이션 HTML 문자열을 생성하여 반환하는 공용 함수
 * @param {object} pageData - 서버로부터 받은 Pagination 객체
 * @returns {string} - 생성된 페이지네이션 HTML 문자열
 */
function renderPagination(pageData) {
    // 전체 페이지가 1페이지 이하면 아무것도 반환하지 않음
    if (!pageData || pageData.totalPages <= 1) {
        return '';
    }

    let html = '<div class="pagination-container mt-5"><nav><ul class="pagination justify-content-center">';

    // '처음' & '이전' 버튼
    html += pageData.currentPage > 1 
        ? `<li class="page-item"><a class="page-link" href="#" data-page="1">처음</a></li>` 
        : '<li class="page-item disabled"><a class="page-link" href="#">처음</a></li>';
    html += pageData.currentPage > 1
        ? `<li class="page-item"><a class="page-link" href="#" data-page="${pageData.currentPage - 1}">이전</a></li>`
        : '<li class="page-item disabled"><a class="page-link" href="#">이전</a></li>';
    
    // 페이지 번호 버튼들
    for (let i = pageData.startPage; i <= pageData.endPage; i++) {
        html += i === pageData.currentPage
            ? `<li class="page-item active"><a class="page-link" href="#">${i}</a></li>`
            : `<li class="page-item"><a class="page-link" href="#" data-page="${i}">${i}</a></li>`;
    }

    // '다음' & '마지막' 버튼
    html += pageData.currentPage < pageData.totalPages
        ? `<li class="page-item"><a class="page-link" href="#" data-page="${pageData.currentPage + 1}">다음</a></li>`
        : '<li class="page-item disabled"><a class="page-link" href="#">다음</a></li>';
    html += pageData.currentPage < pageData.totalPages
        ? `<li class="page-item"><a class="page-link" href="#" data-page="${pageData.totalPages}">마지막</a></li>`
        : '<li class="page-item disabled"><a class="page-link" href="#">마지막</a></li>';

    html += '</ul></nav></div>';

    // 완성된 HTML 문자열을 반환
    return html;
}
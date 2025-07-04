package ks55team02.admin.common.domain;

import lombok.Data;

@Data
public class Pagination {

	private int currentPage;          // 현재 페이지 번호
    private int totalRecordCount;     // 전체 데이터 개수
    private int recordSize;           // 페이지당 출력할 데이터 개수 (pageSize)
    private int blockSize;            // 화면 하단에 출력할 페이지 번호 개수 (예: 5 또는 10)

    private int totalPageCount;       // 전체 페이지 개수
    private int startPage;            // 페이지 블록의 시작 페이지 번호
    private int endPage;              // 페이지 블록의 끝 페이지 번호
    
    private int limitStart;           // MyBatis(SQL)의 LIMIT 구문에서 사용할 시작 인덱스 (OFFSET)
    
    private boolean existPrevBlock;   // 이전 페이지 블록(<<) 존재 여부
    private boolean existNextBlock;   // 다음 페이지 블록(>>) 존재 여부

    public Pagination(int totalRecordCount, SearchCriteria searchCriteria) {
        this.totalRecordCount = totalRecordCount;
        this.currentPage = searchCriteria.getCurrentPage();
        this.recordSize = searchCriteria.getPageSize(); // UserList 객체에서 pageSize 값을 가져와 recordSize에 설정
        this.blockSize = 5; 							// 하단 페이지 번호는 5개씩 보여주기로 결정 (1 2 3 4 5)

        // 생성자 호출 시, 모든 페이지네이션 정보를 계산
        paginationInfo();
    }

    private void paginationInfo() {
        // 1. 전체 페이지 수 계산
        totalPageCount = (int) Math.ceil((double) totalRecordCount / recordSize);

        // 2. 현재 페이지 번호 유효성 체크 (1보다 작거나, 전체 페이지 수보다 크면 보정)
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (currentPage > totalPageCount && totalPageCount > 0) {
            currentPage = totalPageCount;
        }

        // 3. 시작 페이지 번호 계산
        startPage = ((currentPage - 1) / blockSize) * blockSize + 1;

        // 4. 끝 페이지 번호 계산
        endPage = startPage + blockSize - 1;

        // 5. 끝 페이지가 전체 페이지 수보다 크면, 끝 페이지를 전체 페이지 수로 보정
        if (endPage > totalPageCount) {
            endPage = totalPageCount;
        }

        // 6. LIMIT 시작 위치(OFFSET) 계산
        limitStart = (currentPage - 1) * recordSize;
        
        // 7. 이전 페이지 블록 존재 여부
        existPrevBlock = startPage > 1;

        // 8. 다음 페이지 블록 존재 여부
        existNextBlock = endPage < totalPageCount;
    }
}

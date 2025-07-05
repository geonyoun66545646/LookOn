package ks55team02.util;

import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CustomerPagination<T> {

    private List<T> list;            // 현재 페이지의 데이터 목록
    private int currentPage;         // 현재 페이지 번호
    private int totalPages;          // 전체 페이지 수
    private long totalCount;         // 전체 아이템 개수
    private int pageSize;            // 한 페이지에 보여줄 아이템 개수

    private int startPage;           // 페이지네이션 블록의 시작 페이지
    private int endPage;             // 페이지네이션 블록의 끝 페이지
    private boolean hasPreviousBlock; // 이전 페이지 블록 존재 여부
    private boolean hasNextBlock;     // 다음 페이지 블록 존재 여부

    // 생성자
    public CustomerPagination(List<T> list, long totalCount, int currentPage, int pageSize, int blockSize) {
        this.list = list;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;

        // 전체 페이지 수 계산
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 페이지네이션 블록 계산
        this.startPage = ((currentPage - 1) / blockSize) * blockSize + 1;
        this.endPage = Math.min(startPage + blockSize - 1, totalPages);

        this.hasPreviousBlock = this.startPage > 1;
        this.hasNextBlock = this.endPage < this.totalPages;
    }
}
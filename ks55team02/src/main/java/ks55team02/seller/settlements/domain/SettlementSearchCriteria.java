package ks55team02.seller.settlements.domain;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class SettlementSearchCriteria {
	private String storeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> filterConditions;
    private int currentPage = 1;
    private int pageSize = 10;

    private int offset;
    private int limit; // ⭐⭐ 추가: MyBatis LIMIT 절에 사용될 limit 필드 ⭐⭐

    private String searchKey;   // ⭐⭐ 추가: 검색 기준 (예: 'productName') ⭐⭐
    private String searchValue; // ⭐⭐ 추가: 검색어 ⭐⭐

//    @Override
//    public String toString() {
//        return "SettlementSearchCriteria{" +
//               "storeId='" + storeId + '\'' +
//               ", startDate=" + startDate +
//               ", endDate=" + endDate +
//               ", filterConditions=" + filterConditions +
//               ", currentPage=" + currentPage +
//               ", pageSize=" + pageSize +
//               ", offset=" + offset +
//               ", limit=" + limit + // toString에도 추가
//               ", searchKey='" + searchKey + '\'' + // toString에도 추가
//               ", searchValue='" + searchValue + '\'' + // toString에도 추가
//               '}';
//    }
}
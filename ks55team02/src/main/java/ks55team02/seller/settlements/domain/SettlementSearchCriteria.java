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
//               '}';
//    }
}

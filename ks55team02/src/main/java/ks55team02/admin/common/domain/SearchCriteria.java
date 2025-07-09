package ks55team02.admin.common.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class SearchCriteria {

	private int currentPage = 1;
    private int pageSize = 10;
    private int offset;
    private String sortOrder;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String searchKey;
    private String searchValue;
    private List<String> filterConditions;
    
    // ⭐ 추가: 카테고리 레벨 필터링 (예: [1, 2])
    private List<Integer> levels; 
}

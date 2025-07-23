package ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class AdminProductSearchCriteria {
    private String searchKey; // 검색 키 (예: gdsNm, selUserNo, storeName)
    private String searchValue; // 검색어

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; // 검색 시작일

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // 검색 종료일
    
    // [추가] '전체 상품 관리' 페이지의 상태 필터링을 위한 필드
    private String statusFilter; 
}
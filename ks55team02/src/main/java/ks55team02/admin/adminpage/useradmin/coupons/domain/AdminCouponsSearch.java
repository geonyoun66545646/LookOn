package ks55team02.admin.adminpage.useradmin.coupons.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import ks55team02.admin.common.domain.SearchCriteria;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AdminCouponsSearch extends SearchCriteria {
    
    // 공통 검색 필드
    private String searchType;
    private String keyword;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate vldBgngDt;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate vldEndDt;

    // '쿠폰 목록 조회' 페이지에서 사용하는 필드
    private List<Integer> activationStatusList;

    // '회원 쿠폰 조회' 페이지에서 사용하는 필드
    private List<String> usedStatusList;


    private String actvtnYn; 
}
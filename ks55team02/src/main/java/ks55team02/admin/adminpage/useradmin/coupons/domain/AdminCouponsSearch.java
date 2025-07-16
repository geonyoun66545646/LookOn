package ks55team02.admin.adminpage.useradmin.coupons.domain;

import java.time.LocalDate;
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
    
    // [변경] 검색 관련 필드를 아래 두 개로 변경합니다.
    private String searchType; // 검색할 컬럼 (예: 쿠폰명, 쿠폰ID)
    private String keyword;    // 사용자가 입력한 검색어

    // 기존 필드들
    private String actvtnYn;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate vldBgngDt;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate vldEndDt;
}
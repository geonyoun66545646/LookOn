package ks55team02.admin.adminpage.useradmin.coupons.domain;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import ks55team02.admin.common.domain.SearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCouponsSearch extends SearchCriteria {
    private String actvtnYn;          // 활성 여부 (1: 활성, 0: 비활성)
    private String issueConditionType; // 발급 조건 타입
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate vldBgngDt;      // 유효 시작일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate vldEndDt;       // 유효 종료일
}
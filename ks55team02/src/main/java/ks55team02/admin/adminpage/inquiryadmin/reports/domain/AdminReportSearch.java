package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import ks55team02.admin.common.domain.SearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminReportSearch extends SearchCriteria {

    private String prcsSttsCd; // 신고 검색에만 필요한 처리 상태 코드
}
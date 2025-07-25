package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import java.util.List;

import ks55team02.admin.common.domain.SearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminReportSearch extends SearchCriteria {

    private String prcsSttsCd; // 신고 검색에만 필요한 처리 상태 
    private List<String> statusList;
    private int recordSize = 10; // 페이지당 출력할 레코드 수 (기본값 10으로 설정)

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }
}
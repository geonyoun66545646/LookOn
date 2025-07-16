package ks55team02.admin.adminpage.useradmin.search.domain;

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
public class AdminSearchLogCriteria extends SearchCriteria {

    private String searchType; // 검색할 컬럼 (예: 검색로그ID, 사용자번호)
    private String keyword;    // 사용자가 입력한 검색어

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate searchStartDate; // 검색 시작일

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate searchEndDate; // 검색 종료일
}
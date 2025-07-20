package ks55team02.admin.adminpage.inquiryadmin.reports.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReport;
import ks55team02.admin.common.domain.SearchCriteria;

@Mapper
public interface AdminReportMapper {

    /**
     * 조건에 맞는 전체 신고 데이터의 개수를 조회
     * @param searchCriteria 검색 조건
     * @return 전체 데이터 개수 (int)
     */
    int getAdminTotalReportCount(SearchCriteria searchCriteria);

    /**
     * 조건에 맞는 신고 목록을 페이징하여 조회
     * @param paramMap 검색 조건(searchCriteria)과 페이징 정보(pagination)가 모두 담긴 맵
     * @return 페이징 처리된 신고 목록
     */
    List<AdminReport> getAdminReportList(Map<String, Object> paramMap);

}
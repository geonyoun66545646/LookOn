package ks55team02.admin.dashboard.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardMapper {

	// 오늘의 매출 조회
    long getTodayRevenue();

    // 오늘 방문자 수 조회
    int getTodayVisitors();

    // 신규 가입자 수 조회
    int getNewUserCount();

    // [처리할 업무] - 상점 신청 건수 조회
    int countPendingStores();

    // [처리할 업무] - 상품 승인 건수 조회
    int countPendingProducts();

    // [처리할 업무] - 신고 접수 건수 조회
    int countPendingReports();
    
    // [처리할 업무] - 정산 대기 건수 조회
    int countPendingSettlements();

    // [처리할 업무] - 문의 접수 건수 조회
    int countPendingInquiry();

    // 월별 통계 데이터 조회 (수수료 수익)
    // MyBatis는 여러 컬럼을 반환할 때 Map<String, Object> 리스트로 받는 것이 편리합니다.
    List<Map<String, Object>> getMonthlyRevenue();
    
}

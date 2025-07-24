package ks55team02.admin.dashboard.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardMapper {

	/**
     * 오늘 발생한 총 매출(수익) 금액을 조회합니다.
     *
     * @return 오늘의 총 매출 금액 (long 타입)
     */
    long getTodayRevenue();

    /**
     * 오늘 웹사이트를 방문한 총 방문자 수를 조회합니다.
     *
     * @return 오늘의 총 방문자 수 (int 타입)
     */
    int getTodayVisitors();

    /**
     * 오늘 새로 가입한 사용자(회원)의 수를 조회합니다.
     *
     * @return 오늘의 신규 가입자 수 (int 타입)
     */
    int getNewUserCount();

    /**
     * 현재 승인 대기 중인 상점 신청 건수를 조회합니다.
     * 이는 관리자가 처리해야 할 업무 중 하나입니다.
     *
     * @return 승인 대기 중인 상점 신청 건수 (int 타입)
     */
    int countPendingStores();

    /**
     * 현재 승인 대기 중인 상품 등록 건수를 조회합니다.
     * 이는 관리자가 처리해야 할 업무 중 하나입니다.
     *
     * @return 승인 대기 중인 상품 등록 건수 (int 타입)
     */
    int countPendingProducts();

    /**
     * 현재 처리 대기 중인 사용자 신고 접수 건수를 조회합니다.
     * 이는 관리자가 처리해야 할 업무 중 하나입니다.
     *
     * @return 처리 대기 중인 신고 접수 건수 (int 타입)
     */
    int countPendingReports();
    
    /**
     * 현재 정산(결제 처리) 대기 중인 건수를 조회합니다.
     * 이는 관리자가 처리해야 할 업무 중 하나입니다.
     *
     * @return 정산 대기 중인 건수 (int 타입)
     */
    int countPendingSettlements();

    /**
     * 현재 답변 대기 중인 고객 문의 접수 건수를 조회합니다.
     * 이는 관리자가 처리해야 할 업무 중 하나입니다.
     *
     * @return 답변 대기 중인 문의 접수 건수 (int 타입)
     */
    int countPendingInquiry();

    /**
     * 월별 총 매출 또는 수수료 수익 데이터를 조회합니다.
     * 각 월의 데이터는 맵 형태로 반환되며, 키는 컬럼 이름(예: "month", "revenue"),
     * 값은 해당 월의 매출(또는 수익) 데이터가 됩니다.
     *
     * @return 각 월의 매출(또는 수익) 정보를 담은 {@code List<Map<String, Object>>}
     * (예: [{"month": "2023-01", "revenue": 100000}, {"month": "2023-02", "revenue": 120000}, ...])
     */
    List<Map<String, Object>> getMonthlyRevenue();
}

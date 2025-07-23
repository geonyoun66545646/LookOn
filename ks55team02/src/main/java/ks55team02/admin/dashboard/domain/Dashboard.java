package ks55team02.admin.dashboard.domain;

import java.util.List;

import lombok.Data;

@Data
public class Dashboard {

	// 1. 상단 카드 데이터
    private long todayRevenue = 0L;
    private int todayVisitors = 0;
    private int newUserCount = 0;
    private int totalPendingTasks = 0; // 처리할 업무 총합

    // 2. '확인 필요 항목' 세부 데이터
    private int pendingStoreCount = 0;
    private int pendingProductCount = 0;
    private int pendingSettlementCount = 0;
    private int pendingReportCount = 0;
    private int pendingInquiryCount = 0; // 나중에 추가할 것을 대비해 미리 만들어 둡니다.

    // 3. 월별 매출 차트 데이터
    private List<String> monthlyLabels;
    private List<Long> monthlyRevenueData;
    private List<Long> monthlyTotalSalesData; // 새로 추가할 총 판매액
    
    private List<Long> monthlyAverageRevenueData;
    private List<Long> monthlyAverageTotalSalesData;
    
	
}

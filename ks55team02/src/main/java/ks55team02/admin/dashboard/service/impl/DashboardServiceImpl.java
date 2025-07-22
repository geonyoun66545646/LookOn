package ks55team02.admin.dashboard.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ks55team02.admin.dashboard.domain.Dashboard;
import ks55team02.admin.dashboard.mapper.DashboardMapper;
import ks55team02.admin.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

	private final DashboardMapper dashboardMapper;

    @Override
    public Dashboard getDashboardData() { // 반환 타입 및 생성 객체 이름 수정
        Dashboard dashboard = new Dashboard();

        // 1. 상단 카드 데이터 조회 및 DTO에 설정
        dashboard.setTodayRevenue(dashboardMapper.getTodayRevenue());
        dashboard.setTodayVisitors(dashboardMapper.getTodayVisitors());
        dashboard.setNewUserCount(dashboardMapper.getNewUserCount());

        // 2. '확인 필요 항목' 세부 데이터 개별 조회
        int pendingStores = dashboardMapper.countPendingStores();
        int pendingProducts = dashboardMapper.countPendingProducts();
        int pendingSettlements = dashboardMapper.countPendingSettlements();
        int pendingReports = dashboardMapper.countPendingReports();
        // int pendingInquiries = dashboardMapper.countPendingInquiries(); // 문의는 담당자 확인 후 주석 해제
        
        dashboard.setPendingStoreCount(pendingStores);
        dashboard.setPendingProductCount(pendingProducts);
        dashboard.setPendingSettlementCount(pendingSettlements);
        dashboard.setPendingReportCount(pendingReports);
        // dashboard.setPendingInquiryCount(pendingInquiries);

        // 3. '처리할 업무' 총합 계산 및 DTO에 설정
        int totalTasks = pendingStores + pendingProducts + pendingSettlements + pendingReports;
        dashboard.setTotalPendingTasks(totalTasks);
        
        // 4. 월별 매출 차트 데이터 조회 및 가공
        List<Map<String, Object>> monthlyRevenueList = dashboardMapper.getMonthlyRevenue();

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        List<Long> totalSalesData = new ArrayList<>();

        for (Map<String, Object> monthlyData : monthlyRevenueList) {
            String monthLabel = ((String) monthlyData.get("month")).substring(5) + "월";
            Long revenueValue = ((Number) monthlyData.get("revenue")).longValue();         
            Long totalSalesValue = ((Number) monthlyData.get("totalSales")).longValue();
            
            labels.add(monthLabel);
            data.add(revenueValue);
            totalSalesData.add(totalSalesValue); // 리스트에 추가
        }

        dashboard.setMonthlyLabels(labels);
        dashboard.setMonthlyRevenueData(data);
        dashboard.setMonthlyTotalSalesData(totalSalesData);
        
        long avgRevenue = 0;
        if (!data.isEmpty()) {
            // !!!! 이 부분이 수정되었습니다. revenueData.size() -> data.size() !!!!
            avgRevenue = data.stream().mapToLong(Long::longValue).sum() / data.size(); 
        }
        List<Long> averageRevenueList = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) { 
            averageRevenueList.add(avgRevenue);
        }
        dashboard.setMonthlyAverageRevenueData(averageRevenueList);

        // 총 판매액 평균 계산
        long avgTotalSales = 0;
        if (!totalSalesData.isEmpty()) {
            // 이 부분은 이미 올바르게 totalSalesData.size()를 사용하고 있었습니다.
            avgTotalSales = totalSalesData.stream().mapToLong(Long::longValue).sum() / totalSalesData.size();
        }
        List<Long> averageTotalSalesList = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) { 
            averageTotalSalesList.add(avgTotalSales);
        }
        dashboard.setMonthlyAverageTotalSalesData(averageTotalSalesList);
        
        return dashboard;
    }
    
}

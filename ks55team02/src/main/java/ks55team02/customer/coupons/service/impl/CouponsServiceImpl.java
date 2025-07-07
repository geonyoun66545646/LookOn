package ks55team02.customer.coupons.service.impl;

import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.customer.coupons.mapper.CouponsMapper;
import ks55team02.customer.coupons.service.CouponsService;
import ks55team02.util.CustomerPagination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponsServiceImpl implements CouponsService {

    private final CouponsMapper couponsMapper;
    private static final int PAGE_SIZE = 10;
    private static final int BLOCK_SIZE = 10;

    @Override
    public CustomerPagination<Coupons> getAvailableCoupons(String userNo, String keyword, String sortOrder, int page) {
        // 1. 파라미터 조합
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userNo", userNo);
        paramMap.put("keyword", keyword);
        paramMap.put("sortOrder", sortOrder);

        // 2. 전체 개수 조회
        int totalCount = couponsMapper.getAvailableCouponsCount(paramMap);

        // 3. 페이지네이션 정보 계산 및 파라미터 추가
        int offset = (page - 1) * PAGE_SIZE;
        paramMap.put("limit", PAGE_SIZE);
        paramMap.put("offset", offset);

        // 4. 해당 페이지 목록 조회
        List<Coupons> availableCoupons = couponsMapper.getAvailableCoupons(paramMap);

        // 5. 최종 CustomerPagination 객체 생성 및 반환
        return new CustomerPagination<>(availableCoupons, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
    }

    @Override
    public CustomerPagination<UserCoupons> getMyCoupons(String userNo, String keyword, String sortOrder, int page) {
        // 위 getAvailableCoupons와 동일한 로직으로 보유 쿠폰 목록을 조회합니다.
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userNo", userNo);
        paramMap.put("keyword", keyword);
        paramMap.put("sortOrder", sortOrder);
        
        int totalCount = couponsMapper.getMyCouponsCount(paramMap);

        int offset = (page - 1) * PAGE_SIZE;
        paramMap.put("limit", PAGE_SIZE);
        paramMap.put("offset", offset);
        
        List<UserCoupons> myCoupons = couponsMapper.getMyCoupons(paramMap);
        
        return new CustomerPagination<>(myCoupons, totalCount, page, PAGE_SIZE, BLOCK_SIZE);
    }

    @Override
    @Transactional // 여러 DB 작업을 하므로 트랜잭션 처리가 중요합니다.
    public boolean issueCouponToUser(String userNo, String couponId) {
        
        // 1. 발급 전, 쿠폰의 정책 정보를 DB에서 미리 조회합니다.
        Coupons couponInfo = couponsMapper.getCouponInfo(couponId);
        if (couponInfo == null) {
            // 존재하지 않는 쿠폰이면 실패 처리 (혹은 예외 처리)
            return false; 
        }

        // 2. 발급 조건(issue_condition_type)에 따라 자격 조건을 검사합니다.
        String condition = couponInfo.getIssueConditionType();
        
        if ("FIRST_PURCHASE".equals(condition)) {
            // "첫 구매" 쿠폰일 경우: 이 사용자의 주문 횟수를 확인합니다.
            // TODO: 사용자의 전체 주문 횟수를 조회하는 orderMapper.getOrderCount(userNo) 같은 메서드 필요
            // int orderCount = orderMapper.getOrderCount(userNo); 
            // if (orderCount > 0) {
            //     // 주문 기록이 있으면 첫 구매가 아니므로 발급 실패
            //     return false;
            // }
        } else if ("WEEKEND_ONLY".equals(condition)) {
            // "주말" 쿠폰일 경우: 오늘이 정말 주말인지 확인합니다.
            // Calendar cal = Calendar.getInstance();
            // int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1:일요일, 7:토요일
            // if (dayOfWeek != 1 && dayOfWeek != 7) {
            //     return false; // 주말이 아니면 발급 실패
            // }
        }
        // TODO: 생일, VIP 등급 등 다른 조건들도 여기에 'else if'로 추가할 수 있습니다.


        // 3. 재발급 주기(reissue_cycle)에 따라 중복 여부를 검사합니다.
        String reissueCycle = couponInfo.getReissueCycle();
        int issuedCount = 0;

        if ("NONE".equals(reissueCycle)) {
            // 재발급 불가 쿠폰: 전체 기간 동안 받은 적 있는지 확인
            issuedCount = couponsMapper.countUserCouponByCouponId(userNo, couponId);
            if (issuedCount > 0) return false;

        } else if ("WEEKLY".equals(reissueCycle)) {
            // 주간 재발급 쿠폰: 이번 주에 받은 적 있는지 확인
            // TODO: "이번 주에" 이 쿠폰을 받은 기록이 있는지 확인하는 쿼리(예: countCouponIssuedThisWeek)를 Mapper에 추가하고 호출해야 합니다.
            // issuedCount = couponsMapper.countCouponIssuedThisWeek(userNo, couponId);
            // if (issuedCount > 0) return false;
        }
        // TODO: MONTHLY 등 다른 재발급 정책도 여기에 추가...
        

        // 4. 모든 검사를 통과했다면, 쿠폰을 발급합니다.
        String nextUserCouponId = couponsMapper.getNextUserCouponId();
        UserCoupons newUserCoupon = new UserCoupons();
        newUserCoupon.setUserCpnId(nextUserCouponId);
        newUserCoupon.setUserNo(userNo);
        newUserCoupon.setPblcnCpnId(couponId);
        newUserCoupon.setIndivExpryDt(couponInfo.getVldEndDt());
        
        couponsMapper.issueUserCoupon(newUserCoupon);
        
        return true; // 최종 성공
    }
}
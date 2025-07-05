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
    public CustomerPagination<Coupons> getAvailableCoupons(String userId, String keyword, String sortOrder, int page) {
        // 1. 파라미터 조합
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
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
    public CustomerPagination<UserCoupons> getMyCoupons(String userId, String keyword, String sortOrder, int page) {
        // 위 getAvailableCoupons와 동일한 로직으로 보유 쿠폰 목록을 조회합니다.
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
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
    public boolean issueCouponToUser(String userId, String couponId) {
        // TODO: 여기에 쿠폰 발급 전 유효성 검사(예: 이미 발급받았는지, 수량이 남았는지 등) 로직 추가
        
        UserCoupons newUserCoupon = new UserCoupons();
        newUserCoupon.setUserId(userId);
        newUserCoupon.setPblcnCpnId(couponId);
        // ... 그 외 필요한 정보 설정 ...
        
        couponsMapper.issueUserCoupon(newUserCoupon);
        
        return true; // 성공 시 true 반환
    }
}
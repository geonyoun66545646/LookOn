package ks55team02.customer.coupons.mapper;

import ks55team02.util.CustomerPagination;
import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface CouponsMapper {

    /**
     * 발급 가능한 쿠폰 목록 조회
     */
    List<Coupons> getAvailableCoupons(Map<String, Object> paramMap);

    /**
     * 발급 가능한 쿠폰의 전체 개수 조회
     */
    int getAvailableCouponsCount(Map<String, Object> paramMap);

    /**
     * 보유한 쿠폰 목록 조회
     */
    List<UserCoupons> getMyCoupons(Map<String, Object> paramMap);

    /**
     * 보유한 쿠폰의 전체 개수 조회
     */
    int getMyCouponsCount(Map<String, Object> paramMap);
    
    /**
     * 특정 쿠폰의 정보를 조회 (쿠폰 발급 전 조건 확인용)
     */
    Coupons getCouponInfo(String couponId);
    
    /**
     * 사용자가 특정 쿠폰을 이미 보유하고 있는지 확인
     */
    int countUserCouponByCouponId(String userNo, String couponId);

    /**
     * 사용자에게 쿠폰 발급 (user_coupons 테이블에 INSERT)
     */
    void issueUserCoupon(UserCoupons userCoupon);

}
package ks55team02.customer.coupons.mapper;

import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface CouponsMapper {

    /* =================================================================== */
    /* 발급 가능한 쿠폰 (쿠폰 받기 탭) */
    /* =================================================================== */

    /**
     * 현재 사용자가 발급받을 수 있는 쿠폰 목록을 조회하는 쿼리
     * @param paramMap 로그인한 사용자 ID, 정렬, 페이징 정보 등이 담긴 Map
     * @return 발급 가능한 쿠폰 목록
     */
    List<Coupons> getAvailableCoupons(Map<String, Object> paramMap);

    /**
     * 발급 가능한 쿠폰의 전체 개수를 조회하는 쿼리
     * @param paramMap 로그인한 사용자 ID, 검색어 등이 담긴 Map
     * @return 발급 가능한 쿠폰의 총 개수
     */
    int getAvailableCouponsCount(Map<String, Object> paramMap);


    /* =================================================================== */
    /* 보유 쿠폰 (보유 쿠폰 탭) */
    /* =================================================================== */

    /**
     * 현재 사용자가 보유한 쿠폰 목록을 조회하는 쿼리
     * @param paramMap 로그인한 사용자 ID, 정렬, 페이징 정보 등이 담긴 Map
     * @return 보유한 쿠폰 목록
     */
    List<UserCoupons> getMyCoupons(Map<String, Object> paramMap);

    /**
     * 보유한 쿠폰의 전체 개수를 조회하는 쿼리
     * @param paramMap 로그인한 사용자 ID, 검색어 등이 담긴 Map
     * @return 보유한 쿠폰의 총 개수
     */
    int getMyCouponsCount(Map<String, Object> paramMap);


    /* =================================================================== */
    /* 쿠폰 발급 처리 */
    /* =================================================================== */

    /**
     * 특정 쿠폰을 특정 사용자에게 발급 처리하는 쿼리 (user_coupons 테이블에 INSERT)
     * @param userCoupon 발급할 사용자 ID, 쿠폰 코드 등의 정보가 담긴 객체
     */
    void issueUserCoupon(UserCoupons userCoupon);

}
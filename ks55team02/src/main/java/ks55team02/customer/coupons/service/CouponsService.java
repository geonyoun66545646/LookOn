package ks55team02.customer.coupons.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.customer.coupons.domain.Coupons;
import ks55team02.customer.coupons.domain.UserCoupons;
import ks55team02.util.CustomerPagination;

@Service
public interface CouponsService {

    /**
     * 발급 가능한 쿠폰 목록과 페이지 정보를 조회합니다.
     * @param userNo 현재 로그인한 사용자 아이디
     * @param keyword 검색어
     * @param sortOrder 정렬 순서
     * @param page 요청 페이지 번호
     * @return 페이지네이션이 적용된 발급 가능 쿠폰 목록
     */
    CustomerPagination<Coupons> getAvailableCoupons(String userNo, String keyword, String sortOrder, int page);

    /**
     * 보유한 쿠폰 목록과 페이지 정보를 조회합니다.
     * @param userNo 현재 로그인한 사용자 아이디
     * @param keyword 검색어
     * @param sortOrder 정렬 순서
     * @param page 요청 페이지 번호
     * @return 페이지네이션이 적용된 보유 쿠폰 목록
     */
    CustomerPagination<UserCoupons> getMyCoupons(String userNo, String keyword, String sortOrder, int page);

    /**
     * 사용자가 쿠폰을 발급받도록 처리합니다.
     * @param userNo 쿠폰을 발급받을 사용자 아이디
     * @param couponId 발급받을 쿠폰의 ID
     * @return 쿠폰 발급 성공 여부 및 메시지
     */
    boolean issueCouponToUser(String userNo, String couponId);
    
    /**
     * - 2025.07.11 gy -
     * 특정 사용자가 보유한 사용 가능한 쿠폰 목록을 조회합니다.
     * @param userNo 사용자 번호
     * @return 사용 가능한 AdminUserCoupons 객체 리스트
     */
    List<UserCoupons> getUserAvailableCoupons(String userNo);

}
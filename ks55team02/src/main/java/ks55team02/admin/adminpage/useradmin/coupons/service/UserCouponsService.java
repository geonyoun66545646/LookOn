package ks55team02.admin.adminpage.useradmin.coupons.service; // 본인 프로젝트 경로에 맞게 수정

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;
import java.util.Map;

public interface UserCouponsService {
    /**
     * 회원에게 발급된 쿠폰 목록과 페이지네이션 정보를 조회
     * @param search 검색 조건
     * @return 쿠폰 목록과 페이지네이션 정보가 담긴 Map
     */
    Map<String, Object> getUserCouponsList(AdminCouponsSearch search);
    
 // 서비스에도 동일한 이름의 메소드를 선언합니다.
    void updateUserCouponStatus(String userCpnId);
}
package ks55team02.admin.adminpage.useradmin.coupons.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

// [중요] import하는 AdminUserCoupons 클래스가 admin 패키지의 것인지 확인하세요.
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminUserCoupons; 
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;

@Mapper
public interface UserCouponsMapper {

    // [수정] 반환 타입을 admin 패키지의 UserCoupons로 명시합니다.
    List<AdminUserCoupons> findUserCoupons(AdminCouponsSearch search);
    
    int countUserCoupons(AdminCouponsSearch search);
    
    void deleteUserCoupon(String userCpnId); 
    
    // 이 메소드가 <update id="updateUserCouponStatus"> 와 연결됩니다.
    void updateUserCouponStatus(String userCpnId);
}
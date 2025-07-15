package ks55team02.admin.adminpage.useradmin.coupons.mapper;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminCouponsMapper {

    List<AdminCoupons> getCouponsList(AdminCouponsSearch search);
    int getCouponsCount(AdminCouponsSearch search);
    void addCoupon(AdminCoupons adminCoupons);
    String getLastCouponId();
    void updateCoupon(AdminCoupons adminCoupons);
    AdminCoupons getCouponById(String pblcnCpnId);
    void deleteCoupon(String pblcnCpnId);

    // [중요] 일괄 삭제 메소드 선언. @Param을 추가하여 안정성을 높입니다.
    void batchUpdateActivationStatus(@Param("idList") List<String> idList, @Param("status") int status);
}
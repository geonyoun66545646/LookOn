package ks55team02.admin.adminpage.useradmin.coupons.service.impl; // 본인 프로젝트 경로에 맞게 수정

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminUserCoupons; // 1-1에서 만든 DTO
import ks55team02.admin.adminpage.useradmin.coupons.mapper.UserCouponsMapper; // 1-2에서 만든 Mapper
import ks55team02.admin.adminpage.useradmin.coupons.service.UserCouponsService;
import ks55team02.admin.common.domain.Pagination;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCouponsServiceImpl implements UserCouponsService {

    private final UserCouponsMapper userCouponsMapper;
    
    @Override
    public void updateUserCouponStatus(String userCpnId) {
        userCouponsMapper.updateUserCouponStatus(userCpnId);
    }

    @Override
    public Map<String, Object> getUserCouponsList(AdminCouponsSearch search) {
        // 1. 검색 조건에 해당하는 회원 쿠폰 총 개수 조회
        int totalCount = userCouponsMapper.countUserCoupons(search);

        // 2. 페이지네이션 객체 생성
        Pagination pagination = new Pagination(totalCount, search);
        
        // 3. DB 조회를 위한 offset 계산 및 설정
        search.setOffset((search.getCurrentPage() - 1) * search.getPageSize());

        // 4. 조건에 맞는 회원 쿠폰 목록 조회
        List<AdminUserCoupons> userCouponsList = userCouponsMapper.findUserCoupons(search);
        
        // 5. 컨트롤러에 전달할 Map 객체 조립
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("pagination", pagination);
        resultMap.put("couponsList", userCouponsList); // 키 값은 기존 JS와 맞추기 위해 "couponsList" 사용
        resultMap.put("totalCount", totalCount);

        return resultMap;
        
    }
}
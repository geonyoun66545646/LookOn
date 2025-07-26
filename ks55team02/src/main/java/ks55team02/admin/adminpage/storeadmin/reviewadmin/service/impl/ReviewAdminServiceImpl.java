package ks55team02.admin.adminpage.storeadmin.reviewadmin.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.reviewadmin.mapper.ReviewAdminMapper;
import ks55team02.admin.adminpage.storeadmin.reviewadmin.service.ReviewAdminService;
import ks55team02.common.domain.store.ProductReview;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReviewAdminServiceImpl implements ReviewAdminService {

    private final ReviewAdminMapper reviewAdminMapper;

    @Override
    public int getReviewCount(ProductReview review) {
        log.info("서비스: getReviewCount 호출 - 검색 조건: {}", review);
        return reviewAdminMapper.getReviewCount(review);
    }

    // [수정] 인터페이스에 맞게 메소드 시그니처 수정
    @Override
    public List<ProductReview> getReviewList(ProductReview review) {
        log.info("서비스: getReviewList 호출 - 페이지네이션/검색 조건: {}", review);
        // ProductReview 객체에 이미 offset, pageSize 정보가 있으므로 그대로 넘겨줌
        return reviewAdminMapper.getReviewList(review);
    }

    // [수정] 일괄 업데이트를 위한 새로운 서비스 메소드
    @Override
    public void updateReviewStatusBatch(List<String> reviewIds, String newStatus, String loggedInUserLgnId) {
        log.info("서비스: updateReviewStatusBatch 호출 - 리뷰 ID 개수: {}, 새 상태: {}, 로그인 사용자: {}",
                 reviewIds.size(), newStatus, loggedInUserLgnId);

        String delPrcrUserNo = reviewAdminMapper.getUserNoByUserLgnId(loggedInUserLgnId);
        if (delPrcrUserNo == null) {
            log.warn("로그인된 사용자 ID '{}'에 해당하는 user_no를 찾을 수 없어 처리를 중단합니다.", loggedInUserLgnId);
            throw new IllegalArgumentException("처리자 정보를 찾을 수 없습니다.");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("reviewIds", reviewIds);
        params.put("newStatus", newStatus);
        params.put("delPrcrNo", delPrcrUserNo);

        reviewAdminMapper.updateReviewStatusBatch(params);
        log.info("리뷰 {}개 상태를 '{}'(으)로 일괄 업데이트 완료. 처리자: {}", reviewIds.size(), newStatus, loggedInUserLgnId);
    }
}
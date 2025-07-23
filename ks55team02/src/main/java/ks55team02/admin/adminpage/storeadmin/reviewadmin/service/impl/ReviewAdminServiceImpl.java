package ks55team02.admin.adminpage.storeadmin.reviewadmin.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.common.domain.store.ProductReview; // 임포트 경로 변경
import ks55team02.admin.adminpage.storeadmin.reviewadmin.mapper.ReviewAdminMapper;
import ks55team02.admin.adminpage.storeadmin.reviewadmin.service.ReviewAdminService;
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

    @Override
    public List<ProductReview> getReviewList(ProductReview review, int limitStart, int pageSize) {
        log.info("서비스: getReviewList 호출 - 페이지네이션/검색 조건: {}, limitStart: {}, pageSize: {}", review, limitStart, pageSize);
        return reviewAdminMapper.getReviewList(review, limitStart, pageSize);
    }

    @Override
    public void updateReviewStatus(List<String> reviewIds, Integer newStatus, String loggedInUserLgnId) {
        log.info("서비스: updateReviewStatus 호출 - 리뷰 ID: {}, 새 상태: {}, 로그인 사용자: {}", reviewIds, newStatus, loggedInUserLgnId);
        LocalDateTime now = LocalDateTime.now();

        String delPrcrUserNo = null;
        if (loggedInUserLgnId != null && !loggedInUserLgnId.isEmpty()) {
            delPrcrUserNo = reviewAdminMapper.getUserNoByUserLgnId(loggedInUserLgnId);
            if (delPrcrUserNo == null) {
                log.warn("로그인된 사용자 ID '{}'에 해당하는 user_no를 찾을 수 없습니다. del_prcr_no는 NULL로 설정됩니다.", loggedInUserLgnId);
            }
        } else {
            log.warn("로그인 사용자 ID가 제공되지 않았습니다. del_prcr_no는 NULL로 설정됩니다.");
        }

        LocalDateTime delDtToUpdate = null;
        String delPrcrNoToUpdate = null;

        if (newStatus == 0) { // 상태가 '삭제(숨김)'일 경우 (HTML에서 0으로 넘어옴)
            delDtToUpdate = now; // 삭제 일시를 현재 시간으로 설정
            delPrcrNoToUpdate = delPrcrUserNo; // 삭제 처리자 번호 설정
        }

        for (String reviewId : reviewIds) {
            reviewAdminMapper.updateReviewStatus(reviewId, newStatus, now, delDtToUpdate, delPrcrNoToUpdate);
            log.info("리뷰 {} 상태를 {}로 업데이트 완료 (last_mdfcn_dt: {}, del_dt: {}, del_prcr_no: {})",
                     reviewId, newStatus, now, delDtToUpdate, delPrcrNoToUpdate);
        }
    }
}

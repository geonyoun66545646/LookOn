package ks55team02.customer.store.service;

import java.util.List;
import ks55team02.common.domain.store.ProductReview;
import ks55team02.customer.store.domain.ReviewAddDto;
import ks55team02.orderproduct.domain.OrderDTO;

/**
 * 리뷰 관련 비즈니스 로직을 정의하는 인터페이스
 */
public interface ReviewService {

    /* =================================================================== */
    /*                         '좋아요(도움이 돼요)' 관련                    */
    /* =================================================================== */

    /**
     * 리뷰 '좋아요' 상태를 토글합니다.
     * @param reviewId 리뷰 ID
     * @param userNo 사용자 번호
     * @return 작업 후의 '좋아요' 상태 (true: 좋아요, false: 취소)
     */
    boolean toggleReviewLike(String reviewId, String userNo);

    /**
     * 특정 리뷰의 총 '좋아요' 수를 조회합니다.
     * @param reviewId 리뷰 ID
     * @return 총 좋아요 수
     */
    int countReviewLikes(String reviewId);

    /* =================================================================== */
    /*                         리뷰 목록 조회 (페이지네이션)                   */
    /* =================================================================== */

    /**
     * ✅ [통합] 특정 상품의 리뷰 목록을 페이징하여 조회합니다. (사용자 '좋아요' 여부 포함)
     * @param gdsNo 상품 번호
     * @param currentPage 현재 페이지
     * @param pageSize 페이지 당 리뷰 수
     * @param currentUserNo 현재 로그인한 사용자 번호 (비로그인이면 null)
     * @return ProductReview 객체 리스트
     */
    List<ProductReview> getReviewListPageByGdsNo(String gdsNo, int currentPage, int pageSize, String currentUserNo);

    /**
     * 특정 상품의 게시된 리뷰 총 개수를 조회합니다. (페이지네이션용)
     * @param gdsNo 상품 번호
     * @return 리뷰 개수
     */
    long getReviewCountByGdsNo(String gdsNo);

    /* =================================================================== */
    /*                            리뷰 작성 관련                           */
    /* =================================================================== */
 
    /**
     * 새로운 리뷰를 등록합니다.
     * 내부적으로 구매 이력, 중복 작성 여부를 검증하고 파일을 처리합니다.
     * @param reviewAddDto 사용자가 폼에서 작성한 데이터
     * @param currentUserNo 현재 로그인한 사용자의 ID
     */
    void addReview(ReviewAddDto reviewAddDto, String currentUserNo);
 
    /**
     * ✅ [수정] 특정 상품에 대해 리뷰 작성이 가능한 최근 주문 정보를 조회합니다.
     * @param userNo 사용자 번호
     * @param gdsNo 상품 번호
     * @return OrderDTO 객체
     */
    OrderDTO findReviewableOrder(String userNo, String gdsNo);

}
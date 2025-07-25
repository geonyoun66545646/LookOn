package ks55team02.admin.adminpage.storeadmin.reviewadmin.mapper;

import java.util.List;
import java.util.Map; // Map 임포트 추가

import org.apache.ibatis.annotations.Mapper;

import ks55team02.common.domain.store.ProductReview;

@Mapper
public interface ReviewAdminMapper {

    /**
     * 조건에 맞는 리뷰의 총 개수를 조회합니다.
     * @param review 검색 및 필터 조건 DTO
     * @return 총 리뷰 개수
     */
    int getReviewCount(ProductReview review);

    /**
     * [수정] 조건에 맞는 리뷰 목록을 페이징하여 조회합니다.
     * ProductReview 객체 안에 모든 검색/필터/페이징 정보가 담겨 있습니다.
     * @param review 검색, 필터, 페이징 정보 DTO
     * @return 리뷰 목록
     */
    List<ProductReview> getReviewList(ProductReview review);

    /**
     * [추가] 여러 리뷰의 상태를 한 번의 쿼리로 일괄 업데이트합니다.
     * @param params reviewIds(List), newStatus(String), delPrcrNo(String)를 담고 있는 Map
     * @return 업데이트된 행의 수
     */
    int updateReviewStatusBatch(Map<String, Object> params);

    /**
     * 사용자의 로그인 ID를 통해 사용자의 고유 번호(user_no)를 조회합니다.
     * @param userLgnId 로그인 ID
     * @return 사용자 고유 번호(user_no)
     */
    String getUserNoByUserLgnId(String userLgnId);
}
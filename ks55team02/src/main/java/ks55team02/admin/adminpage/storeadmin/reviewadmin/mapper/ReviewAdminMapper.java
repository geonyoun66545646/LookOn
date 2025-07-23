package ks55team02.admin.adminpage.storeadmin.reviewadmin.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.common.domain.store.ProductReview; // 임포트 경로 변경

@Mapper
public interface ReviewAdminMapper {

    int getReviewCount(ProductReview review);

    List<ProductReview> getReviewList(@Param("review") ProductReview review,
                                      @Param("limitStart") int limitStart,
                                      @Param("pageSize") int pageSize);

    int updateReviewStatus(@Param("reviewId") String reviewId,
                           @Param("newStatus") Integer newStatus, // HTML에서 0, 1로 넘어오므로 Integer 유지
                           @Param("lastMdfcnDt") LocalDateTime lastMdfcnDt,
                           @Param("delDt") LocalDateTime delDt,
                           @Param("delPrcrNo") String delPrcrNo);

    String getUserNoByUserLgnId(@Param("userLgnId") String userLgnId);
}

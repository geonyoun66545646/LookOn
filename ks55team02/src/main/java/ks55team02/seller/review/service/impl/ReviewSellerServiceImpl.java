/*
 * package ks55team02.seller.review.service.impl;
 * 
 * import java.util.List;
 * 
 * import org.springframework.stereotype.Service; import
 * org.springframework.transaction.annotation.Transactional;
 * 
 * import ks55team02.common.domain.store.ProductReview; import
 * ks55team02.seller.review.mapper.ReviewSellerMapper; import
 * ks55team02.seller.review.service.ReviewSellerService; import
 * lombok.RequiredArgsConstructor; import lombok.extern.log4j.Log4j2;
 * 
 * @Service
 * 
 * @Transactional
 * 
 * @RequiredArgsConstructor
 * 
 * @Log4j2 public class ReviewSellerServiceImpl implements ReviewSellerService{
 * 
 * private final ReviewSellerMapper reviewSellerMapper;
 * 
 * @Override public int getSellerReviewCount(ProductReview review) {
 * log.info("서비스: getReviewCount 호출 - 검색 조건: {}", review); return
 * reviewSellerMapper.getSellerReviewCount(review); }
 * 
 * // [수정] 인터페이스에 맞게 메소드 시그니처 수정
 * 
 * @Override public List<ProductReview> getSellerReviewList(ProductReview
 * review) { log.info("서비스: getReviewList 호출 - 페이지네이션/검색 조건: {}", review); //
 * ProductReview 객체에 이미 offset, pageSize 정보가 있으므로 그대로 넘겨줌 return
 * reviewSellerMapper.getSellerReviewList(review); }
 * 
 * }
 */
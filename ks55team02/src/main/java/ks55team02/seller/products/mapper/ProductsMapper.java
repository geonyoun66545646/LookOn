package ks55team02.seller.products.mapper;

import ks55team02.admin.adminpage.productadmin.adminproductsmanagement.domain.ProductApprovalHistory;
import ks55team02.seller.products.domain.*;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductsMapper {
	
	
	// --- 상품 등록(Create) 관련 메소드 ---
	String getMaxProductCode();
	String getMaxImageNo();
	String getMaxOptionNo();
	String getMaxOptionValueNo();
	String getMaxStatusNo();
	void addProduct(Products product);
	void insertProductImage(ProductImage productImage);
	void insertProductOption(ProductOption option);
	void insertProductOptionValue(ProductOptionValue value);
	void insertProductStatus(ProductStatus productStatus);
	void insertStatusOptionMapping(StatusOptionMapping mapping);
	
	// ⭐⭐⭐⭐⭐ [추가 시작] ⭐⭐⭐⭐⭐

    /**
     * 특정 이미지의 순서(img_indct_sn)를 업데이트합니다.
     * @param imgNo 순서를 변경할 이미지의 고유 ID
     * @param order 새로 지정할 순번
     */
    void updateImageOrder(String imgNo, int order);

    /**
     * 특정 상품의 특정 타입에 해당하는 모든 이미지를 조회합니다.
     * @param gdsNo 상품의 고유 ID
     * @param imageType 조회할 이미지 타입 (THUMBNAIL, MAIN, DETAIL)
     * @return 해당 타입의 이미지 목록
     */
    List<ProductImage> getProductImagesByGdsNoAndType(String gdsNo, ProductImageType imageType);

    /**
     * 특정 상품의 특정 타입에 해당하는 모든 이미지를 삭제합니다. (주로 썸네일 교체 시 사용)
     * @param gdsNo 상품의 고유 ID
     * @param imageType 삭제할 이미지 타입
     */
    void deleteImagesByGdsNoAndType(String gdsNo, ProductImageType imageType);

    // ⭐⭐⭐⭐⭐ [추가 끝] ⭐⭐⭐⭐⭐
	
	// 새로 추가: 최근 반려된 이력 조회
    ProductApprovalHistory getLatestRejectedHistory(String gdsNo);
    
    // 새로 추가: 특정 상품의 최신 승인/반려 차수 조회 (대기 상태 제외)
    Integer getLatestProcessedCycle(String gdsNo);
	
	// 새로운 승인 이력 PK를 가져오는 메소드
	String getMaxApprovalHistoryCode();

	// 최초 승인 이력을 추가하는 메소드
	void addInitialApprovalHistory(Products product);
	
	// 특정상품 모든 이밎 조회
	List<ProductImage> getProductImagesByGdsNo(String gdsNo);
	
	/* */
	void deactivateProductsByCategoryId(String categoryId);

	// 상품 수정
	/**
	 * 상품의 기본 정보를 수정합니다.
	 * 
	 * @param product 수정할 정보가 담긴 Products 객체
	 */
	void updateProduct(Products product);
	
	// 수정시 기존정보 삭제
	void deleteStatusOptionMappingsByGdsNo(String gdsNo);
	void deleteOptionValuesByGdsNo(String gdsNo);
	void deleteImagesByGdsNo(String gdsNo);
    void deleteOptionsByGdsNo(String gdsNo);
    void deleteStatusByGdsNo(String gdsNo);
    void deleteImagesByImageNos(List<String> imageNoList);

	// -- 상품 삭제
	/**
	 * 특정 상품을 비활성화(소프트 삭제) 처리합니다.
	 * 
	 * @param paramMap 'gdsNo', 'selUserNo' (삭제 요청자)를 포함한 맵
	 */
	void deactivateProduct(Map<String, Object> paramMap);

	// --- 상품 조회(Read) 관련 메소드 (주로 판매자/관리자용) ---

	/**
	 * 특정 판매자의 특정 스토어에 속한 모든 상품 목록을 조회합니다.
	 * 
	 * @param paramMap 'selUserNo', 'storeId'를 포함한 맵
	 * @return 상품 목록
	 */
	List<Products> getProductsBySellerAndStore(Map<String, Object> paramMap);

	/**
	 * (관리자용) 시스템의 모든 상품 목록을 조회합니다.
	 * 
	 * @return 모든 상품 목록
	 */
	List<Products> getProductList();

	/**
	 * 특정 상품의 모든 상세 정보를 연관된 객체(이미지, 옵션 등)와 함께 조회합니다.
	 * 
	 * @param gdsNo 조회할 상품의 ID
	 * @return 상품 상세 정보 객체
	 */
	Products getProductDetailWithImages(String gdsNo);

	// --- 유틸리티 메소드 ---

	/**
	 * 특정 상품 코드가 이미 데이터베이스에 존재하는지 확인합니다.
	 * 
	 * @param productCode 확인할 상품 코드
	 * @return 존재하면 1 이상, 없으면 0
	 */
	int countProductCode(String productCode);
}
package ks55team02.seller.products.service;

import java.util.List;
import java.util.Map;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.seller.products.domain.ProductOptionValue;
import ks55team02.seller.products.domain.ProductRegistrationRequest;
import ks55team02.seller.products.domain.Products;
import ks55team02.seller.stores.domain.Stores;

/**
 * 상품 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다. 판매자 상품 관리, 상품 등록, 조회 등 다양한 상품 관련 기능을 제공합니다.
 */
public interface ProductsService {

	/**
	 * 특정 상품의 모든 옵션 조합(gds_stts_no)과 그에 속한 옵션 값(opt_vl_no)들을 조회
	 * 
	 * @param gdsNo 상품 코드
	 * @return 옵션 조합과 값 매핑 정보 리스트
	 */
	public List<Map<String, Object>> getProductStatusOptions(String gdsNo);

	/**
	 * 키워드로 브랜드를 검색합니다.
	 * 
	 * @param keyword 검색어
	 * @return 검색된 브랜드 목록
	 */
	List<Stores> searchBrands(String keyword);

	/**
	 * 특정 상품을 비활성화(소프트 삭제) 합니다.
	 * 
	 * @param gdsNo     비활성화할 상품 ID
	 * @param selUserNo 작업을 요청한 판매자 ID
	 */
	void deactivateProduct(String gdsNo, String selUserNo);

	/**
	 * 기존 상품의 정보를 수정합니다.
	 * 
	 * @param request 수정할 상품 정보가 담긴 DTO
	 */
	void updateProduct(ProductRegistrationRequest request);

	/**
	 * ⭐ [수정] 특정 판매자 번호와 스토어 ID, 그리고 검색 조건에 해당하는 상품 목록을 조회합니다.
	 * @param searchCriteria 검색 조건 (판매자 ID, 상점 ID 포함)
	 * @return 상품 목록과 페이지네이션 정보가 담긴 Map
	 */
	Map<String, Object> getProductsBySellerAndStore(SearchCriteria searchCriteria);

	// 새로운 상품을 시스템에 등록합니다.
	void addProduct(ProductRegistrationRequest request);

	// 특정 상품 코드가 이미 데이터베이스에 존재하는지 확인합니다.
	boolean isProductCodeDuplicated(String productCode);

	// (관리자용) 모든 상품 목록을 조회합니다.
	List<Products> getProductList();
	

	// 특정 상품의 상세 정보를 조회하고, 연관된 이미지 및 카테고리 정보까지 반환합니다.
	Products getProductDetailWithImages(String gdsNo);

	// 모든 상품 컬러 옵션 값을 조회합니다.
	List<ProductOptionValue> getAllProductColors();

	// 모든 의류 표준 사이즈 옵션 값을 조회합니다.
	List<ProductOptionValue> getAllApparelSizes();

	// 모든 신발 표준 사이즈 옵션 값을 조회합니다.
	List<ProductOptionValue> getAllShoeSizes();

	// ⭐ 모든 패션 소품 표준 사이즈 옵션 값을 조회합니다. ⭐
	List<ProductOptionValue> getAllFashionSizes();

	// 모든 브랜드(상점) 목록을 조회합니다.
	List<Stores> getAllBrands();

	/**
	 * 특정 상점 ID로 상점 정보를 조회합니다. 이 메서드는 StoreMapper의 getStoreById를 호출합니다.
	 * 
	 * @param storeId 상점 고유 ID
	 * @return 해당 ID의 Stores 객체 (삭제 처리되지 않은 상점만)
	 */
	Stores getStoreByStoreId(String storeId);
}
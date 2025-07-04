package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.*;
import ks55team02.seller.stores.domain.Stores; // ⭐ 추가: Stores 도메인 임포트 (올바른 경로)
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper // 이 인터페이스가 MyBatis 매퍼임을 나타냅니다.
public interface ProductsMapper {

    // ----------- 기존 상품 등록/관리 관련 메서드 -----------

    // 판매자 번호(selUserNo)와 스토어 ID(storeId)에 해당하는 상품 목록을 조회합니다.
    List<Products> getProductsBySellerAndStore(Map<String, Object> paramMap);

    // 새 상품 코드(gdsNo)를 생성하기 위한 최대 상품 코드를 조회합니다.
    String getMaxProductCode();

    // 상품 기본 정보를 PRODUCTS 테이블에 삽입합니다.
    void addProduct(Products product);

    // 새 이미지 번호(imgNo)를 생성하기 위한 최대 이미지 번호를 조회합니다.
    String getMaxImageNo();

    // 상품 이미지 정보를 PRODUCT_IMAGE 테이블에 삽입합니다.
    void insertProductImage(ProductImage productImage);

    // 새 옵션 번호(optNo)를 생성하기 위한 최대 옵션 번호를 조회합니다.
    String getMaxOptionNo();

    // 상품 옵션 정보를 PRODUCT_OPTION 테이블에 삽입합니다.
    void insertProductOption(ProductOption option);

    // 새 옵션 값 번호(optVlNo)를 생성하기 위한 최대 옵션 값 번호를 조회합니다.
    String getMaxOptionValueNo();

    // 상품 옵션 값 정보를 PRODUCT_OPTION_VALUE 테이블에 삽입합니다.
    void insertProductOptionValue(ProductOptionValue value);

    // 새 상품 상태 번호(gdsSttsNo)를 생성하기 위한 최대 상품 상태 번호를 조회합니다.
    String getMaxStatusNo();

    // 상품 상태(재고) 정보를 PRODUCT_STATUS 테이블에 삽입합니다.
    void insertProductStatus(ProductStatus productStatus);

    // 상품 상태와 옵션 값 매핑 정보를 STATUS_OPTION_MAPPING 테이블에 삽입합니다.
    void insertStatusOptionMapping(StatusOptionMapping mapping);

    // 특정 상품 코드가 이미 데이터베이스에 존재하는지 여부를 카운트합니다.
    int countProductCode(String productCode);

    // 모든 상품 목록을 조회합니다 (관리자/판매자용).
    List<Products> getProductList();

    // 고객에게 노출될 수 있는, 활성화된 모든 상품 목록을 조회합니다.
    List<Products> getAllActiveProductsForCustomer();

    // 고객용: 특정 카테고리에 속하는 활성화 및 노출 상품 목록을 조회합니다.
    List<Products> getActiveProductsForCustomerByCategory(String categoryId);

    // 특정 상품의 상세 정보를 조회하고, 연관된 이미지 정보를 함께 반환합니다.
    Products getProductDetailWithImages(String gdsNo);

    // ----------- 새로운 고객용 상품 조회/필터링 관련 메서드 -----------

    // 카테고리 ID로 단일 카테고리 정보를 조회합니다.
    ProductCategory getCategoryById(String categoryId);

    // 부모 카테고리 ID에 해당하는 하위 카테고리 목록을 조회합니다.
    List<ProductCategory> getSubCategoriesByParentId(String parentCategoryId);

    // 모든 최상위 카테고리 (부모가 없는) 목록을 조회합니다.
    List<ProductCategory> getAllTopLevelCategories();

    // 최상위 카테고리 이름으로 카테고리 ID를 조회합니다.
    String getTopLevelCategoryIdByName(String categoryName);

    // 모든 상품 카테고리 목록을 조회합니다.
    List<ProductCategory> getAllProductCategories();

    // 다양한 필터 및 정렬 조건에 따라 상품 목록을 조회합니다.
    List<Products> getFilteredAndSortedProducts(Map<String, Object> paramMap);

    // 다양한 필터 및 정렬 조건에 해당하는 상품의 총 개수를 조회합니다.
    long countFilteredProducts(Map<String, Object> paramMap);

    // 특정 옵션 유형(예: "컬러", "의류 사이즈", "신발 사이즈")에 해당하는 모든 옵션 값들을 조회합니다.
    List<ProductOptionValue> getAllProductOptionValuesByType(String optionTypeName);

    /**
     * ⭐ 새로 추가: 특정 옵션 유형에 해당하는 vl_nm만 중복 없이 조회 (String 리스트 반환) ⭐
     * 이 메서드는 getAllProductColors()에서 사용되어 색상 중복을 방지합니다.
     * @param optionTypeName 옵션 유형 이름 (예: "색상")
     * @return 중복 제거된 vl_nm (String) 리스트
     */
    List<String> getDistinctProductOptionValueNamesByType(String optionTypeName); // <--- 이 줄이 추가되었습니다!

    // 모든 브랜드(상점) 목록을 조회합니다.
    List<Stores> getAllStores();
}
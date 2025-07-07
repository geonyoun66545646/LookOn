package ks55team02.seller.products.mapper;

import ks55team02.seller.products.domain.ProductOptionValue;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ProductOptionMapper {

    /**
     * 특정 옵션 유형(예: "색상", "사이즈")에 해당하는 모든 옵션 값들을 조회합니다.
     * @param optionTypeName 옵션 유형 이름
     * @return 해당 옵션 유형의 옵션 값 목록
     */
    List<ProductOptionValue> getAllProductOptionValuesByType(String optionTypeName);

    /**
     * 특정 옵션 유형에 해당하는 옵션 값 이름(vl_nm)만 중복 없이 조회합니다.
     * @param optionTypeName 옵션 유형 이름 (예: "색상")
     * @return 중복 제거된 옵션 값 이름(String) 목록
     */
    List<String> getDistinctProductOptionValueNamesByType(String optionTypeName);
}
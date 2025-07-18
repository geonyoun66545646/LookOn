package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data // 이 어노테이션이 있어야 getter/setter가 자동으로 생성됩니다.
public class ProductCategory {
    private String categoryId;       // ctgry_no 와 매핑 (카테고리 번호)
    private String parentCategoryId; // up_ctgry_no 와 매핑 (상위 카테고리 번호)
    private String userNo;           // creatr_no 와 매핑 (생성자 회원 번호)
    private String categoryName;     // ctgry_nm 와 매핑 (카테고리 명)
    private Integer categoryLevel;   // ctgry_dpth 와 매핑 (카테고리 깊이)
    private LocalDateTime regDate;   // reg_dt 와 매핑 (등록일)
    private Boolean activationYn;    // actvtn_yn 와 매핑 (활성화 여부) - ⭐ 이 필드가 추가되었습니다. ⭐
    private String categoryImagePath;
}
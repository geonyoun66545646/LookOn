package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data // 이 어노테이션이 있어야 getter/setter가 자동으로 생성됩니다.
public class ProductCategory {
    private String categoryId;       // ctgry_no 와 매핑
    private String parentCategoryId; // up_ctgry_no 와 매핑
    private String userNo;           // creatr_no 와 매핑
    private String categoryName;     // ctgry_nm 와 매핑
    private Integer categoryLevel;   // ctgry_dpth 와 매핑
    private LocalDateTime regDate;   // reg_dt 와 매핑
}
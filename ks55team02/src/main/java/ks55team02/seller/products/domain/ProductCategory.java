// ProductCategory.java

package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductCategory {
    private String categoryId;       // ctgry_no
    private String parentCategoryId; // up_ctgry_no
    private String userNo;           // creatr_no
    private String mdfrNo;           // mdfr_no  ⭐ 이 필드를 추가합니다.
    private String delUserNo;        // del_user_no ⭐ 이 필드를 추가합니다.
    private String categoryName;     // ctgry_nm
    private Integer categoryLevel;   // ctgry_dpth
    private LocalDateTime regDate;   // reg_dt
    private Boolean activationYn;    // actvtn_yn
    private String categoryImagePath; // ctgry_img_path
}
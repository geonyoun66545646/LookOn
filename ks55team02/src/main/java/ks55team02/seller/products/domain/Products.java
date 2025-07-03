package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal; // Add this import for BigDecimal

@Data
public class Products {
    private String gdsNo;
    private String storeId;
    private String ctgryNo;
    private String selUserNo;
    private String creatrNo;
    private String mdfrNo;
    private String delUserNo;
    private String gdsNm;
    private String gdsExpln;
    private Integer basPrc;
    private Double dscntRt;
    private Integer lastSelPrc; // Consider if this should be BigDecimal as well
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private LocalDateTime inactvtnDt;
    private Boolean expsrYn;
    private Boolean actvtnYn;
    private String thumbnailImagePath;
    private Integer minPurchaseQty;
    private Integer maxPurchaseQty;

    private String storeName;

    private Integer totalStockQuantity;
    private Boolean isSoldOut;

    // ⭐⭐⭐ MISSING FIELD: discountedPrice - ADD THIS! ⭐⭐⭐
    private BigDecimal discountedPrice; // Or Double, but BigDecimal is recommended for currency.

    private ProductCategory productCategory;
    private List<ProductImage> productImages;
    private List<ProductOption> productOptions;

    // These getters are redundant with @Data but won't cause harm.
    public Boolean getExpsrYn() {
        return this.expsrYn;
    }
    public Boolean getActvtnYn() {
        return this.actvtnYn;
    }
}
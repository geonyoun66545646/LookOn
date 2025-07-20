package ks55team02.seller.products.domain;

import ks55team02.seller.stores.domain.Stores;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal; // Add this import for BigDecimal

@Data
public class Products {
	private Stores store; 
	
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
    private Integer lastSelPrc;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private LocalDateTime inactvtnDt;
    private Boolean expsrYn;
    private Boolean actvtnYn;
    private String thumbnailImagePath;
    private Integer minPurchaseQty;
    private Integer maxPurchaseQty;

    private String storeName;
    
    private String latestApprovalStatus;
    
    private String aprvRjctHstryCd; 
    
    private Integer totalStockQuantity;
    private Boolean isSoldOut;

    private BigDecimal discountedPrice;

    private ProductCategory productCategory;
    private List<ProductImage> productImages;
    private List<ProductOption> productOptions;
    
   
    public Boolean getExpsrYn() {
        return this.expsrYn;
    }
    public Boolean getActvtnYn() {
        return this.actvtnYn;
    }
    
    private List<ProductStatus> productStatusList;
}
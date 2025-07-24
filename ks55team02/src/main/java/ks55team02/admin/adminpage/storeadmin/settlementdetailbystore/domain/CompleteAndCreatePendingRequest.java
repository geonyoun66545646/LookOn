package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CompleteAndCreatePendingRequest {
	private String completedStoreClclnId;
    private String newStoreId;
    private String newPlcyId;
    private String newActnoId;
    private BigDecimal newTotSelAmt;
}

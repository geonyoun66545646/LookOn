package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain;

import lombok.Data;

@Data
public class CdfpDTO {
	private String plcyId;      // 정책 ID
    private String plcyExpln;   // 정책 설명
    private Integer feeRt;      // 수수료율 (%) - 정수
}

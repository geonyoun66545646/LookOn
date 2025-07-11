package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // 부모 클래스 필드까지 고려하여 equals/hashCode 생성
public class StoreSettlementListViewDTO extends StoreSettlementDTO {
	private String storeNm; // 상점명 (stores 테이블에서 조인하여 가져올 필드)
    private String plcyNm;  // 정책명 (cdfp 테이블에서 조인하여 가져올 필드)
    // 이 DTO는 StoreSettlement의 모든 필드를 상속받고 storeNm만 추가합니다.
}

package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StoreAccountDTO {
	private String actnoId;      // 계좌 ID
    private String storeId;      // 상점 아이디
    private String bankNm;       // 은행 명
    private String actno;        // 계좌 번호
    private String dpstrNm;      // 예금 주주
    private boolean mainActnoYn; // 주계좌 여부
    private LocalDateTime crtDt; // 생성 일시
    private boolean actvtnYn;    // 활성 여부
    private LocalDateTime delDt; // 삭제(비활성) 일시
    private String delUserNo;    // 삭제(비활성)자 번호
}

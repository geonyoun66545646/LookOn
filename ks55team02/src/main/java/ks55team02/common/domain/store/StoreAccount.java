package ks55team02.common.domain.store;

import java.time.LocalDateTime;

import lombok.Data; // Lombok 사용 시

@Data // @Getter, @Setter, @EqualsAndHashCode, @ToString 등을 포함
public class StoreAccount {
    private String actnoId; // 계좌 ID
    private String storeId; // 상점 아이디 (store_application의 aply_id와 연결)
    private String bankNm; // 은행 명
    private Long actno; // 계좌 번호
    private String dpstrNm; // 예금 주주
    private boolean mainActnoYn; // 주계좌 여부 (tinyint(1)은 boolean으로 매핑)
    private LocalDateTime crtDt; // 생성 일시
    private boolean actvtnYn; // 활성 여부
    private LocalDateTime delDt; // 삭제(비활성) 일시
    private String delUserNo; // 삭제(비활성)자 번호
}

package ks55team02.common.enums;

import lombok.Getter;

@Getter // Lombok을 사용하여 getDisplayName()을 자동으로 생성
public enum ApplyStatus {
    APPLY("APPLY", "접수"),
    PENDING("PENDING", "보류"),
    APPROVED("APPROVED", "승인"),
    REJECTED("REJECTED", "반려");

    private final String code;
    private final String displayName;

    ApplyStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    // 코드 값으로 Enum 상수를 찾는 정적 메소드 (매우 유용)
    public static String getDisplayNameByCode(String code) {
        for (ApplyStatus status : ApplyStatus.values()) {
            if (status.getCode().equals(code)) {
                return status.getDisplayName();
            }
        }
        return "알 수 없음"; // 혹은 null, 또는 예외 처리
    }
}
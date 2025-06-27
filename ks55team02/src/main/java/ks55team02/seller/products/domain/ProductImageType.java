package ks55team02.seller.products.domain; // 📌 실제 프로젝트의 패키지 경로가 맞는지 다시 한번 확인해주세요!

public enum ProductImageType {
    THUMBNAIL(1, "썸네일 이미지"),
    MAIN(2, "대표 이미지"),
    DETAIL(3, "상세 이미지");

    private final int code;
    private final String description;

    ProductImageType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // DB의 정수(code) 값을 Enum 타입으로 변환하기 위한 정적 팩토리 메서드
    public static ProductImageType fromCode(int code) {
        for (ProductImageType type : ProductImageType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        // 유효하지 않은 코드가 들어올 경우 예외 처리
        throw new IllegalArgumentException("Invalid ProductImageType code: " + code);
    }
}
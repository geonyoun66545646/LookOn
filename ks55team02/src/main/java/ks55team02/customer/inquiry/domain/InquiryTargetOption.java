package ks55team02.customer.inquiry.domain;

/*문의 대상을 지정*/
public enum InquiryTargetOption {
    STORE("상점"),
    ADMIN("관리자");

    private final String targetOption;

    InquiryTargetOption(String targetOption) {
        this.targetOption = targetOption;
    }

    public String getTargetOption() {
        return targetOption;
    }
}
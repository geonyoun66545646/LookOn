package ks55team02.customer.inquiry.domain;


public enum InquiryOption {
		PRODUCT("상품"),
	    DELIVERY("배송"),
	    STORE("상점"),
	    SNS("SNS"),
	    ETC("기타");

	    private final String inquiryOption;

	    InquiryOption(String inquiryOption) {
	        this.inquiryOption = inquiryOption;
	    }

	    public String getInquiryOption() {
	        return inquiryOption;
	    }

}

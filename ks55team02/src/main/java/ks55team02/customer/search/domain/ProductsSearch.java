package ks55team02.customer.search.domain;

import lombok.Data;

@Data
public class ProductsSearch {
	private String gdsNo;
    private String gdsNm;
    private int lastSelPrc;
    private String imgFilePathNm;
}

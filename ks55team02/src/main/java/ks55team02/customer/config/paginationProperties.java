package ks55team02.customer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties("pagination")
@Component
@Data
public class paginationProperties {
	
    private int defaultSize = 10; // 기본값 설정

    private String pageParameterName = "page"; // 기본값 설정
    private String sizeParameterName = "size"; // 기본값 설정

    // Getter 및 Setter 메서드도 변경된 필드 이름에 맞게 수정해야 합니다.
    public int getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }

    public String getPageParameterName() {
        return pageParameterName;
    }

    public void setPageParameterName(String pageParameterName) {
        this.pageParameterName = pageParameterName;
    }

    public String getSizeParameterName() {
        return sizeParameterName;
    }

    public void setSizeParameterName(String sizeParameterName) {
        this.sizeParameterName = sizeParameterName;
    }
}

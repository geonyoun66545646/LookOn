package ks55team02.customer.brnoapi.domain;

import java.util.List;

import lombok.Data;

@Data
public class NtsApiRequestDto {
    private List<String> b_no; // 국세청 API는 'b_no' 필드명으로 사업자번호 리스트를 받습니다.

    public NtsApiRequestDto(List<String> b_no) {
        this.b_no = b_no;
    }
}
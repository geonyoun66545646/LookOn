package ks55team02.customer.brnoApi.domain;

import lombok.Data; // Lombok의 @Data 어노테이션을 사용하여 getter, setter, toString 등을 자동 생성합니다.
import java.util.List;

@Data // @Data 어노테이션은 @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor를 포함합니다.
public class NtsApiResponseDto {
    private String request_cnt; // 요청 건수
    private String valid_cnt;   // 유효 건수
    private List<NtsApiResultDto> data; // 실제 결과 데이터 리스트
    private ErrorDetail error; // API 호출 실패 시 에러 정보를 담을 필드

    /**
     * 국세청 API 응답 데이터의 개별 항목을 정의하는 내부 클래스입니다.
     * 각 사업자등록번호에 대한 상세 상태 정보를 담습니다.
     */
    @Data
    public static class NtsApiResultDto {
        private String b_no;      // 사업자등록번호
        private String b_stt;     // 사업자 상태 (예: 계속사업자, 휴업자, 폐업자 등)
        private String b_stt_cd;  // 사업자 상태 코드
        private String tax_type;  // 과세 유형 (예: 일반과세자, 간이과세자, 면세사업자 등)
        private String tax_type_cd; // 과세 유형 코드
        private String end_dt;    // 폐업일자 (YYYYMMDD 형식)
        private String utcc_yn;   // 단위과세전환여부
        private String tax_type_change_dt; // 과세유형전환일자 (YYYYMMDD 형식)
        private String etax_bill_issu_dt; // 전자세금계산서 발급의무일자 (YYYYMMDD 형식)
    }

    /**
     * API 호출 실패 시 에러 정보를 담을 내부 클래스입니다.
     */
    @Data
    public static class ErrorDetail {
        private String message; // 에러 메시지
        // private String code; // 필요에 따라 에러 코드 필드를 추가할 수 있습니다.
    }

    /**
     * 조회 결과 데이터가 비어있거나 에러가 없는지 확인하는 헬퍼 메서드입니다.
     * Thymeleaf 등 템플릿 엔진에서 조건부 렌더링에 유용합니다.
     * @return 데이터가 없거나 에러가 없는 경우 true, 그렇지 않으면 false
     */
    public boolean isEmpty() {
        return (data == null || data.isEmpty()) && error == null;
    }

    /**
     * 응답에 에러 정보가 포함되어 있는지 확인하는 헬퍼 메서드입니다.
     * @return 에러가 있는 경우 true, 그렇지 않으면 false
     */
    public boolean hasError() {
        return error != null;
    }

    /**
     * 응답에 유효한 결과 데이터가 포함되어 있는지 확인하는 헬퍼 메서드입니다.
     * @return 데이터가 비어있지 않은 경우 true, 그렇지 않으면 false
     */
    public boolean hasElement() {
        return data != null && !data.isEmpty();
    }
}
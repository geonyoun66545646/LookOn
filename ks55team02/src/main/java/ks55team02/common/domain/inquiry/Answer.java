package ks55team02.common.domain.inquiry;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Answer {
	
	private String 					ansId; // 답변 ID
    private String 					inqryId; // 문의 ID
    private String 					answrUserNo; // 답변자 사용자 번호
    private String 					ansCn; // 답변 내용
    private LocalDateTime 			ansTm; // 답변 시간
    private String 					answrTypeCd; // 답변자 유형 코드
    private boolean 				rlsYn; // 공개 유무 (tinyint(1)은 boolean으로 매핑)
    private LocalDateTime 			lastMdfcnDt; // 최종 수정일시
    private boolean 				actvtnYn; // 활성 여부 (tinyint(1)은 boolean으로 매핑)
    private String 					delDt; // 삭제(비활성) 일시 (LocalDateTime으로 변경 고려, 현재는 String으로 유지)
    private String 					delUserNo; // 삭제한 관리자 번호

    // 답변자의 스토어 이름을 저장할 필드 추가
    private String 					answrStoreName;

}

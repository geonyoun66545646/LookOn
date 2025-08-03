package ks55team02.seller.inquiry.mapper;

import org.apache.ibatis.annotations.Mapper;
import ks55team02.common.domain.inquiry.Answer;

@Mapper
public interface AnswerMapper {

    // 새로운 답변을 등록합니다.
    int insertAnswer(Answer answer);

    // 기존 답변을 수정합니다.
    int updateAnswer(Answer answer);

    // 특정 답변 ID로 답변 정보를 조회합니다. (수정 후 최신 정보 반환을 위해 필요)
    Answer getAnswerById(String ansId);

    /**
     * `ans_` 접두사를 가진 `ans_id` 중 가장 큰 숫자 부분을 조회합니다.
     * @return 가장 큰 숫자 부분 (없으면 null 또는 0)
     */
    Integer getMaxAnsIdNumber(); // Integer로 반환하여 null 처리 가능하도록
    
}
package ks55team02.systems.batch;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SanctionBatchMapper {

    // 제재가 만료된 사용자 번호 목록 조회
    List<String> findUsersWithExpiredSanctions();
    
    // 여러 사용자의 상태를 '활동'으로 일괄 변경
    int updateUsersStatusToActive(List<String> userNoList);
}
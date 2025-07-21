package ks55team02.seller.settlements.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import ks55team02.seller.settlements.domain.SettlementDTO;
import ks55team02.seller.settlements.domain.SettlementSearchCriteria;

@Mapper
public interface SettlementMapper {
	List<SettlementDTO> selectMySettlementList(SettlementSearchCriteria criteria);

    int countMySettlementList(SettlementSearchCriteria criteria);
    
    String selectStoreIdByUserNo(@Param("userNo") String userNo); // userNo를 파라미터로 받아 store_id 반환
}

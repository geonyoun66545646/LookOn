package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param; // 이 부분을 org.springframework.data.repository.query.Param 대신 사용해야 합니다.

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.CdfpDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;

@Mapper
public interface StoreSettlementMapper {
	/**
     * 전체 정산 정보 개수를 조회합니다. (페이지네이션을 위해 필요)
     * @return 전체 정산 정보 개수
     */
    int getTotalSettlementCount();

    /**
     * 모든 상점의 정산 정보 목록을 페이지네이션과 함께 조회합니다.
     * @param offset LIMIT 시작 위치
     * @param pageSize 페이지당 레코드 수
     * @return StoreSettlementListViewDTO 목록
     */
    List<StoreSettlementListViewDTO> getAllStoreSettlementsForList(
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 특정 상점의 정산 내역을 조회합니다.
     * @param storeId 조회할 상점 ID
     * @return StoreSettlement 목록
     */
    List<StoreSettlementDTO> getSettlementHistoryByStoreId(@Param("storeId") String storeId);

    /**
     * 새로운 정산 정보를 삽입합니다.
     * @param storeSettlement 삽입할 정산 정보
     * @return 삽입된 행의 수
     */
    int insertStoreSettlement(StoreSettlementDTO storeSettlement);

    /**
     * 현재 가장 큰 store_clcln_id의 숫자 부분을 찾아 다음 ID를 생성합니다.
     * (예: 'clcln_00001' -> 'clcln_00002')
     * @return 가장 큰 ID의 숫자 부분
     */
    Integer getMaxStoreClclnSeq();

    /**
     * 특정 정산 ID의 상태를 업데이트합니다.
     * @param storeClclnId 정산 ID
     * @param clclnSttsCd 변경할 정산 상태 코드
     * @return 업데이트된 행의 수
     */
    int updateSettlementStatus(@Param("storeClclnId") String storeClclnId,
                               @Param("clclnSttsCd") String clclnSttsCd);

    /**
     * cdfp 테이블에서 정책 ID에 해당하는 수수료율 정보를 조회합니다.
     * @param plcyId 정책 ID
     * @return CdfpDTO 객체
     */
    CdfpDTO getCdfpByPlcyId(@Param("plcyId") String plcyId);

    /**
     * 특정 storeClclnId에 해당하는 StoreSettlementDTO를 조회합니다.
     * @param storeClclnId 조회할 정산 ID
     * @return StoreSettlementDTO 객체
     */
    StoreSettlementDTO getStoreSettlementById(@Param("storeClclnId") String storeClclnId);

    /**
     * 여러 정산 ID의 상태를 일괄 업데이트합니다.
     * @param storeClclnIds 업데이트할 정산 ID 목록
     * @param clclnSttsCd 변경할 정산 상태 코드
     * @return 업데이트된 행의 수
     */
    int updateSettlementStatusBatch(@Param("storeClclnIds") List<String> storeClclnIds,
                                    @Param("clclnSttsCd") String clclnSttsCd);

    /**
     * 특정 상점의 주 계좌 정보를 조회합니다.
     * @param storeId 상점 ID
     * @return StoreAccountDTO 객체
     */
    StoreAccountDTO getStoreAccountDetailsByStoreId(@Param("storeId") String storeId);
}
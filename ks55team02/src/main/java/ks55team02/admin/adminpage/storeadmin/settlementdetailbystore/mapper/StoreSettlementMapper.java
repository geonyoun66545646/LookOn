package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.CdfpDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreAccountDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementDTO;
import ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain.StoreSettlementListViewDTO;
import ks55team02.admin.common.domain.SearchCriteria; // SearchCriteria 임포트

@Mapper
public interface StoreSettlementMapper {
    /**
     * 전체 정산 정보 개수를 조회합니다. (페이지네이션을 위해 필요)
     * 검색 조건을 적용합니다.
     * @param searchCriteria 검색 조건
     * @return 전체 정산 정보 개수
     */
    int getTotalSettlementCount(SearchCriteria searchCriteria); // SearchCriteria 파라미터 추가

    /**
     * 모든 상점의 정산 정보 목록을 페이지네이션, 검색 및 필터링과 함께 조회합니다.
     * @param searchCriteria 검색 및 페이지네이션 조건 (offset, pageSize, searchKey, searchValue, filterConditions 포함)
     * @return StoreSettlementListViewDTO 목록
     */
    List<StoreSettlementListViewDTO> getAllStoreSettlementsForList(SearchCriteria searchCriteria); // SearchCriteria 파라미터로 변경

    /**
     * 새로운 정산 대기 항목을 삽입합니다.
     * @param storeSettlement 삽입할 정산 DTO
     * @return 삽입된 행의 수
     */
    int insertStoreSettlement(StoreSettlementDTO storeSettlement);

    /**
     * 현재 가장 큰 store_clcln_id의 숫자 부분을 찾아 다음 ID를 생성합니다.
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
     * 특정 상점의 주 계좌 정보를 조회합니다.
     * @param storeId 상점 ID
     * @return StoreAccountDTO 객체
     */
    StoreAccountDTO getStoreAccountDetailsByStoreId(@Param("storeId") String storeId);

    /**
     * 특정 상점의 정산 내역을 조회합니다.
     * @param storeId 조회할 상점 ID
     * @return StoreSettlement 목록
     */
    List<StoreSettlementDTO> getSettlementHistoryByStoreId(@Param("storeId") String storeId);

    // 참고: updateSettlementStatusBatch 메서드는 매퍼에 필요 없습니다.
    // ServiceImpl에서 개별 updateSettlementStatus를 반복 호출하는 방식으로 처리합니다.
}
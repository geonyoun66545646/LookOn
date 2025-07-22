package ks55team02.admin.adminpage.storeadmin.storemngadmin.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.storemngadmin.mapper.StoreMngAdminMapper;
import ks55team02.admin.adminpage.storeadmin.storemngadmin.service.StoreMngAdminService;
import ks55team02.common.domain.store.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@RequiredArgsConstructor // Lombok이 생성자 주입을 자동으로 처리 (private final 필드에 대해)
@Log4j2
public class StoreMngAdminServiceImpl implements StoreMngAdminService {

    private final StoreMngAdminMapper storeMngAdminMapper;

    // @RequiredArgsConstructor 어노테이션을 사용했으므로, 명시적인 생성자 선언은 생략 가능합니다.
    // 만약 RequiredArgsConstructor를 사용하지 않는다면 아래와 같이 생성자를 선언해야 합니다.
    /*
    public StoreMngAdminServiceImpl(StoreMngAdminMapper storeMngAdminMapper) {
        this.storeMngAdminMapper = storeMngAdminMapper;
    }
    */

    /**
     * 상점 목록 전체 개수 조회 (검색 조건 포함)
     * @param store 검색 조건을 담고 있는 Store 객체 (SearchCriteria 상속 가정)
     * @return 전체 상점 개수
     */
    @Override
    public int getStoreCount(Store store) {
        log.info("서비스: getStoreCount 호출 - 검색 조건: {}", store);
        return storeMngAdminMapper.getStoreCount(store);
    }

    /**
     * 상점 목록 조회 (페이지네이션 및 검색 조건 포함)
     * @param store 페이지네이션 및 검색 조건을 담고 있는 Store 객체 (SearchCriteria 상속 가정)
     * @param limitStart LIMIT 구문의 시작 인덱스
     * @param pageSize LIMIT 구문의 가져올 레코드 개수
     * @return 상점 목록
     */
    @Override
    public List<Store> getStoreList(Store store, int limitStart, int pageSize) {
        log.info("서비스: getStoreList 호출 - 페이지네이션/검색 조건: {}, limitStart: {}, pageSize: {}", store, limitStart, pageSize);
        return storeMngAdminMapper.getStoreList(store, limitStart, pageSize);
    }
    
    
    @Override
    public void updateStoreStatus(List<String> storeIds, String newStatus) {
        log.info("서비스: updateStoreStatus 호출 - 상점 ID: {}, 새 상태: {}", storeIds, newStatus);
        LocalDateTime now = LocalDateTime.now();

        // del_prcr_yn 값 초기화
        String delPrcrYnToUpdate = "0"; // 기본값: 폐쇄 아님

        switch (newStatus) {
            case "ACTIVE":
            case "INACTIVE": // INACTIVE도 del_prcr_yn은 '0'
                // del_prcr_yn은 '0' (기본값)
                break;
            case "CLOSED":
                delPrcrYnToUpdate = "1"; // 폐쇄 컬럼 1로 설정
                break;
            default:
                log.warn("알 수 없는 상점 상태: {}", newStatus);
                return; // 처리하지 않음
        }

        for (String storeId : storeIds) {
            // inactvtnDtToUpdate 파라미터 제거
            storeMngAdminMapper.updateStoreStatus(storeId, newStatus, now, delPrcrYnToUpdate);
            log.info("상점 {} 상태를 {}로 업데이트 완료 (info_mdfcn_dt: {}, del_prcr_yn: {})",
                     storeId, newStatus, now, delPrcrYnToUpdate);
        }
    }
}

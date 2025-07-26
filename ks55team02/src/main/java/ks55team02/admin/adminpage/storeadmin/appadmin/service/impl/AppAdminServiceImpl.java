package ks55team02.admin.adminpage.storeadmin.appadmin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.appadmin.mapper.AppAdminMapper;
import ks55team02.admin.adminpage.storeadmin.appadmin.service.AppAdminService;
import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.common.domain.store.AppStore;
import ks55team02.common.domain.store.Store;
import ks55team02.common.domain.store.StoreAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppAdminServiceImpl implements AppAdminService {

    private final AppAdminMapper appAdminMapper;

    @Override
    @Transactional(readOnly = true)
    public int getAppAdminCount(SearchCriteria searchCriteria) {
        return appAdminMapper.getAppAdminCount(searchCriteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppStore> getAppAdminList(SearchCriteria searchCriteria, int limitStart, int pageSize) {
        return appAdminMapper.getAppAdminList(searchCriteria, limitStart, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public AppStore getAppAdminById(String aplyId) {
        return appAdminMapper.getAppAdminById(aplyId);
    }

    @Override
    public void processApplicationStatus(String aplyId, String statusToUpdate, String reason) {
        log.info("상점 신청 처리 시작: aplyId={}, status={}, reason='{}'", aplyId, statusToUpdate, reason);
        
        String dbStatus;
        if ("승인".equals(statusToUpdate) || "APPROVED".equals(statusToUpdate)) {
            dbStatus = "APPROVED";
        } else if ("반려".equals(statusToUpdate) || "REJECTED".equals(statusToUpdate)) {
            dbStatus = "REJECTED";
        } else {
            throw new IllegalArgumentException("지원하지 않는 상태 값입니다: " + statusToUpdate);
        }

        AppStore updateRequest = new AppStore();
        updateRequest.setAplyId(aplyId);
        updateRequest.setAplyStts(dbStatus);
        updateRequest.setAprvRjctRsn(reason);
        appAdminMapper.updateAppStatus(updateRequest);
        log.info("상태 업데이트 완료: aplyId={}, newStatus={}", aplyId, dbStatus);

        if ("APPROVED".equals(dbStatus)) {
            log.info("승인 후처리 시작: aplyId={}", aplyId);
            AppStore approvedApp = appAdminMapper.getAppAdminById(aplyId);
            if (approvedApp == null) {
                throw new IllegalStateException("승인 처리할 신청 정보를 찾을 수 없습니다: " + aplyId);
            }

            Integer maxStoreNum = appAdminMapper.selectMaxStoreIdNum();
            int nextStoreNum = (maxStoreNum == null) ? 1 : maxStoreNum + 1;
            String newStoreId = "store_" + nextStoreNum;

            Store newStore = new Store();
            newStore.setStoreId(newStoreId);
            newStore.setAplyId(approvedApp.getAplyId());
            newStore.setSellerUserNo(approvedApp.getAplyUserNo());
            newStore.setStoreConm(approvedApp.getStoreNm());
            newStore.setStoreIntroCn(approvedApp.getStoreIntroCn());
            newStore.setStoreLogoImg(approvedApp.getStoreLogoImg());
            appAdminMapper.insertStore(newStore);
            log.info("`stores` 테이블에 데이터 삽입 완료: {}", newStoreId);

            Integer maxAccountNum = appAdminMapper.selectMaxAccountIdNum();
            int nextAccountNum = (maxAccountNum == null) ? 1 : maxAccountNum + 1;
            String newAccountId = "account_" + nextAccountNum;

            StoreAccount newAccount = new StoreAccount();
            newAccount.setActnoId(newAccountId);
            newAccount.setStoreId(newStore.getStoreId());
            newAccount.setBankNm(approvedApp.getBankNm());
            newAccount.setActno(approvedApp.getActno());
            newAccount.setDpstrNm(approvedApp.getDpstrNm());
            appAdminMapper.insertStoreAccount(newAccount);
            log.info("`store_account` 테이블에 데이터 삽입 완료: {}", newAccountId);
            
            String sellerUserNo = approvedApp.getAplyUserNo();
            String sellerGradeCode = "grd_cd_2";
            appAdminMapper.updateUserGrade(sellerUserNo, sellerGradeCode);
            log.info("사용자 등급 업데이트 완료 - 사용자 번호: {}, 새 등급: {}", sellerUserNo, sellerGradeCode);
        }
    }
}
package ks55team02.admin.adminpage.storeadmin.appAdmin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.appAdmin.mapper.AppAdminMapper;
import ks55team02.admin.adminpage.storeadmin.appAdmin.service.AppAdminService;
import ks55team02.common.domain.store.AppStore;
import ks55team02.common.domain.store.Store;
import ks55team02.common.domain.store.StoreAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class AppAdminServiceImpl implements AppAdminService{
	
	private final AppAdminMapper appAdminMapper;
	
	
/**
 * 상점 신청 전체 개수 조회 (검색 조건 포함)
 * @param appAdmin 검색 조건을 담고 있는 AppAdmin 객체 (SearchCriteria 상속)
 * @return 전체 상점 신청 개수
 */
@Override // AppAdminService 인터페이스의 메서드를 오버라이드합니다.
public int getAppAdminCount(AppStore appAdmin) {
    log.info("서비스: getAppAdminCount 호출 - 검색 조건: {}", appAdmin);
    return appAdminMapper.getAppAdminCount(appAdmin);
}

/**
 * 상점 신청 목록 조회 (페이지네이션 및 검색 조건 포함)
 * @param appAdmin 페이지네이션 및 검색 조건을 담고 있는 AppAdmin 객체 (SearchCriteria 상속)
 * @return 상점 신청 목록
 */
 // AppAdminService 인터페이스의 메서드를 오버라이드합니다.
@Override // AppAdminService 인터페이스의 메서드를 오버라이드합니다.
public List<AppStore> getAppAdminList(AppStore appAdmin, int limitStart, int pageSize) { //
    log.info("서비스: getAppAdminList 호출 - 페이지네이션/검색 조건: {}, limitStart: {}, pageSize: {}", limitStart, pageSize); //
    // 매퍼 호출 시 limitStart와 pageSize를 함께 전달
    return appAdminMapper.getAppAdminList(appAdmin, limitStart, pageSize); //
}

/**
 * 특정 상점 신청 상세 조회
 * @param aplyId 신청 ID
 * @return AppAdmin 상세 정보
 */
@Override // AppAdminService 인터페이스의 메서드를 오버라이드합니다.
public AppStore getAppAdminById(String aplyId) {
    log.info("서비스: getAppAdminById 호출 - aplyId: {}", aplyId);
    return appAdminMapper.getAppAdminById(aplyId);
	}

// 상점 신청 상태 변경 및 반려 사유 삽입
@Override
public void updateAppStatus(AppStore appStore) {
    log.info("서비스: updateAppStatus 호출 - 데이터: {}", appStore);

    // 1. 반려 상태일 경우, 반려 사유가 비어있으면 기본값을 설정합니다.
    if ("REJECTED".equals(appStore.getAplyStts()) &&
        (appStore.getAprvRjctRsn() == null || appStore.getAprvRjctRsn().isBlank())) {

        String defaultReason = "귀하의 상점 신청은 서류 검토 과정에서 [구체적인 부족 내용 예: 사업자등록증 사본 미제출, 필수 정보 누락 등]이 확인되어 반려되었습니다.";
        appStore.setAprvRjctRsn(defaultReason);
        log.info("기본 반려 사유 설정 완료");
    }

    // 2. 승인 상태일 경우, 반려 사유를 null로 설정합니다.
    if ("APPROVED".equals(appStore.getAplyStts())) {
        appStore.setAprvRjctRsn(null);
    }
    
    // 3. 신청 상태와 반려 사유, 수정일을 DB에 업데이트합니다. (공통 로직)
    appAdminMapper.updateAppStatus(appStore);

    // 4. 만약 상태가 '승인(APPROVED)'이라면, 추가적인 생성/업데이트 작업을 수행합니다.
    if ("APPROVED".equals(appStore.getAplyStts())) {
        log.info("승인 후처리 시작: aplyId={}", appStore.getAplyId());

        // 4-1. 승인 처리에 필요한 모든 정보를 담고 있는 신청 정보를 다시 조회합니다.
        AppStore applicationInfo = appAdminMapper.getAppAdminById(appStore.getAplyId());
        if (applicationInfo == null) {
            throw new IllegalStateException("승인 처리 중 신청 정보를 찾을 수 없습니다: " + appStore.getAplyId());
        }

        // 4-2. 새로운 상점 ID 생성
        Integer maxStoreNum = appAdminMapper.selectMaxStoreIdNum();
        int nextStoreNum = (maxStoreNum == null) ? 1 : maxStoreNum + 1;
        String newStoreId = "store_" + nextStoreNum;

        // 4-3. 새로운 계좌 ID 생성
        Integer maxAccountNum = appAdminMapper.selectMaxAccountIdNum();
        int nextAccountNum = (maxAccountNum == null) ? 1 : maxAccountNum + 1;
        String newAccountId = "actno_" + nextAccountNum;

        log.info("새로운 ID 생성 완료 - Store ID: {}, Account ID: {}", newStoreId, newAccountId);

        // 4-4. `stores` 테이블에 삽입할 Store 객체 생성
        Store newStore = new Store();
        newStore.setStoreId(newStoreId);
        newStore.setAplyId(applicationInfo.getAplyId());
        newStore.setSellerUserNo(applicationInfo.getAplyUserNo());
        newStore.setStoreConm(applicationInfo.getStoreNm());
        newStore.setStoreIntroCn(applicationInfo.getStoreIntroCn());
        newStore.setStoreLogoImg(applicationInfo.getStoreLogoImg());
        newStore.setStoreIntroCn(applicationInfo.getStoreIntroCn());
        
        appAdminMapper.insertStore(newStore);
        log.info("`stores` 테이블에 데이터 삽입 완료: {}", newStoreId);

        // 4-5. `store_account` 테이블에 삽입할 StoreAccount 객체 생성
        StoreAccount newAccount = new StoreAccount();
        newAccount.setActnoId(newAccountId);
        newAccount.setStoreId(newStoreId);
        newAccount.setBankNm(applicationInfo.getBankNm());
        newAccount.setActno(applicationInfo.getActno());
        newAccount.setDpstrNm(applicationInfo.getDpstrNm());
        

        appAdminMapper.insertStoreAccount(newAccount);
        log.info("`store_account` 테이블에 데이터 삽입 완료: {}", newAccountId);

        // 4-6. 신청자의 사용자 등급을 판매자로 업데이트
        // DTO 구조 변경으로 applicationInfo.getUser().getUserNo()를 사용
        String sellerUserNo = applicationInfo.getUser().getUserNo(); 
        String sellerGradeCode = "grd_cd_2";

        int updatedRows = appAdminMapper.updateUserGrade(sellerUserNo, sellerGradeCode);

        if (updatedRows == 0) {
            throw new RuntimeException("사용자 등급 업데이트에 실패하여 전체 승인 프로세스를 롤백합니다. 사용자 번호: " + sellerUserNo);
        }
        log.info("사용자 등급 업데이트 완료 - 사용자 번호: {}, 새 등급: {}", sellerUserNo, sellerGradeCode);
    }
}

	


}





package ks55team02.customer.store.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.admin.adminpage.useradmin.userlist.mapper.UserListMapper;
import ks55team02.common.domain.store.AppStore;
import ks55team02.common.domain.store.StoreAccount;
import ks55team02.common.domain.store.StoreImage;
import ks55team02.customer.store.mapper.AppStoreMapper;
import ks55team02.customer.store.service.AppStoreService;
import ks55team02.customer.store.service.StoreImageService;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppStoreServiceImpl implements AppStoreService{
	
	private final AppStoreMapper appStoreMapper;
	private final UserListMapper userListMapper;
    private final StoreImageService storeImageService;
    private final FilesUtils filesUtils;

    @Override
    public UserList getUserInfo(String userNo) {
        UserList userList= userListMapper.getUserByUserNo(userNo);
        return userList;
    }

    @Override
    public void addStoreApplication(AppStore appStore, Map<String, MultipartFile> uploadedFiles) {
        // 1. 신청 ID 생성 (가장 큰 숫자 + 1)
        Integer maxAplyIdNum = appStoreMapper.getMaxAplyIdNum();
        int nextAplyIdNum = (maxAplyIdNum != null) ? maxAplyIdNum + 1 : 1; // 기존 ID가 없으면 1부터 시작
        String aplyId = "aply_" + nextAplyIdNum;
        appStore.setAplyId(aplyId); // AppStore 객체에 새로운 신청 ID 설정

        // 2. 이미지 파일 저장 및 이미지 ID 설정
        try {
            String imgId; // 각 파일 업로드 후 반환될 이미지 ID를 저장할 변수

            // 사업자 등록증
            if (uploadedFiles.containsKey("brnoImg") && uploadedFiles.get("brnoImg") != null && !uploadedFiles.get("brnoImg").isEmpty()) {
                imgId = storeImageService.addStoreImage(uploadedFiles.get("brnoImg")); // imgId 반환 받음
                appStore.setBrnoImgId(imgId); // 생성된 이미지 ID를 AppStore에 설정
            }
            // 통신 판매업 신고증
            if (uploadedFiles.containsKey("cmmDclrImg") && uploadedFiles.get("cmmDclrImg") != null && !uploadedFiles.get("cmmDclrImg").isEmpty()) {
                imgId = storeImageService.addStoreImage(uploadedFiles.get("cmmDclrImg"));
                appStore.setCmmDclrImgId(imgId);
            }
            // 판매 상품 관련 증빙
            if (uploadedFiles.containsKey("selGdsProofImg") && uploadedFiles.get("selGdsProofImg") != null && !uploadedFiles.get("selGdsProofImg").isEmpty()) {
                imgId = storeImageService.addStoreImage(uploadedFiles.get("selGdsProofImg"));
                appStore.setSelGdsProofImgId(imgId);
            }
            // 주민등록증 사본
            if (uploadedFiles.containsKey("rrnoCardCopyImg") && uploadedFiles.get("rrnoCardCopyImg") != null && !uploadedFiles.get("rrnoCardCopyImg").isEmpty()) {
                imgId = storeImageService.addStoreImage(uploadedFiles.get("rrnoCardCopyImg"));
                appStore.setRrnoCardCopyImgId(imgId);
            }
            // 통장 사본
            if (uploadedFiles.containsKey("bankbookCopyImg") && uploadedFiles.get("bankbookCopyImg") != null && !uploadedFiles.get("bankbookCopyImg").isEmpty()) {
                imgId = storeImageService.addStoreImage(uploadedFiles.get("bankbookCopyImg"));
                appStore.setBankbookCopyImgId(imgId);
            }
            // 기타 서류
            if (uploadedFiles.containsKey("etcDocImg") && uploadedFiles.get("etcDocImg") != null && !uploadedFiles.get("etcDocImg").isEmpty()) {
                imgId = storeImageService.addStoreImage(uploadedFiles.get("etcDocImg"));
                appStore.setEtcDocImgId(imgId);
            }
            // 상점 로고
            if (uploadedFiles.containsKey("storeLogoImg") && uploadedFiles.get("storeLogoImg") != null && !uploadedFiles.get("storeLogoImg").isEmpty()) {
                imgId = storeImageService.addStoreImage(uploadedFiles.get("storeLogoImg"));
                appStore.setStoreLogoImg(imgId);
                log.info("상점 로고 이미지 ID 생성됨: {}", imgId);
            }

        } catch (Exception e) {
            log.error("파일 저장 중 오류 발생", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
        log.info("store_application 테이블에 INSERT할 데이터: {}", appStore);
        appStoreMapper.addAppStore(appStore);
        log.info("신청 ID {} 에 대한 상점 신청 정보가 성공적으로 저장되었습니다.", appStore.getAplyId());
    }
}

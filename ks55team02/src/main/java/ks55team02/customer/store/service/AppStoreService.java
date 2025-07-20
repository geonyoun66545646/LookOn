package ks55team02.customer.store.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.common.domain.store.AppStore;


public interface AppStoreService {
	
	UserList getUserInfo(String userNo);

    /**
     * 상점 신청 정보를 저장합니다.
     * @param appStore 상점 신청 도메인 객체
     * @param storeAccount 상점 계좌 도메인 객체
     * @param uploadedFiles 업로드된 파일 맵 (파일 종류별 MultipartFile)
     */
    void addStoreApplication(AppStore appStore, Map<String, MultipartFile> uploadedFiles);
}

package ks55team02.admin.adminpage.storeadmin.appAdmin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.appAdmin.domain.AppAdmin;
import ks55team02.admin.adminpage.storeadmin.appAdmin.mapper.AppAdminMapper;
import ks55team02.admin.adminpage.storeadmin.appAdmin.service.AppAdminService;
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
public int getAppAdminCount(AppAdmin appAdmin) {
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
public List<AppAdmin> getAppAdminList(AppAdmin appAdmin, int limitStart, int pageSize) { //
    log.info("서비스: getAppAdminList 호출 - 페이지네이션/검색 조건: {}, limitStart: {}, pageSize: {}", appAdmin, limitStart, pageSize); //
    // 매퍼 호출 시 limitStart와 pageSize를 함께 전달
    return appAdminMapper.getAppAdminList(appAdmin, limitStart, pageSize); //
}

/**
 * 특정 상점 신청 상세 조회
 * @param aplyId 신청 ID
 * @return AppAdmin 상세 정보
 */
@Override // AppAdminService 인터페이스의 메서드를 오버라이드합니다.
public AppAdmin getAppAdminById(String aplyId) {
    log.info("서비스: getAppAdminById 호출 - aplyId: {}", aplyId);
    return appAdminMapper.getAppAdminById(aplyId);
}
}



package ks55team02.admin.adminpage.useradmin.loginhistory.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.adminpage.useradmin.loginhistory.domain.LoginHistory;

@Mapper
public interface LoginHistoryMapper {

	/**
	 * 주어진 검색 및 페이징 조건({@link LoginHistory} 객체에 포함된)에 따라
	 * 사용자의 로그인 기록 목록을 조회합니다.
	 *
	 * @param criteria 검색어, 기간, 페이징(offset, amount) 등 조회 조건을 담은 {@link LoginHistory} 도메인 객체
	 * @return         조건에 맞는 로그인 기록 목록을 담은 {@code List<LoginHistory>}
	 */
	List<LoginHistory> getLoginHistoryList(LoginHistory criteria);
	
    /**
     * 주어진 검색 조건에 맞는 전체 로그인 기록의 수를 조회합니다.
     * 이는 페이징 처리를 위한 총 레코드 수를 계산하는 데 사용됩니다.
     *
     * @param criteria 검색 조건을 담은 {@link LoginHistory} 도메인 객체
     * @return         검색 조건에 맞는 전체 로그인 기록 수 (int 타입)
     */
    int getLoginHistoryCount(LoginHistory criteria);
}

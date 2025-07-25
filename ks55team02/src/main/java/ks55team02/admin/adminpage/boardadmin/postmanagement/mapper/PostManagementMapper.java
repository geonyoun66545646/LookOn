package ks55team02.admin.adminpage.boardadmin.postmanagement.mapper;

import ks55team02.admin.adminpage.boardadmin.postmanagement.domain.AdminPost;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PostManagementMapper {

	/**
	 * 조건에 맞는 게시글 목록을 조회합니다.
	 * 
	 * @param searchCriteria 검색 조건
	 * @return 게시글 목록
	 */
	List<AdminPost> selectPostList(AdminPost searchCriteria);

	/**
	 * 조건에 맞는 전체 게시글의 개수를 조회합니다.
	 * 
	 * @param searchCriteria 검색 조건
	 * @return 전체 게시글 개수
	 */
	int selectPostCount(AdminPost searchCriteria);

	/**
	 * 여러 게시글을 숨김 처리(논리적 삭제)합니다.
	 * 
	 * @param postSns 숨김 처리할 게시글 번호 목록
	 * @return 처리된 행의 수
	 */
	int updatePostsToHidden(List<String> postSns);

	/**
	 * 여러 게시글을 복구 처리합니다.
	 * 
	 * @param postSns 복구할 게시글 번호 목록
	 * @return 처리된 행의 수
	 */
	int updatePostsToRestored(List<String> postSns);
	
	
}
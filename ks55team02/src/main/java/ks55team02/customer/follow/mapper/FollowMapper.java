package ks55team02.customer.follow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FollowMapper {

	/**
     * 특정 사용자 간의 팔로우 관계가 존재하는지 확인합니다. (1이면 존재, 0이면 존재하지 않음)
     * @param followerUserNo 팔로우를 한 사람의 userNo
     * @param followedUserNo 팔로우를 당한 사람의 userNo
     * @return 존재하면 1, 그렇지 않으면 0
     */
    int isFollowing(@Param("followerUserNo") String followerUserNo, @Param("followedUserNo") String followedUserNo);

    /**
     * 새로운 팔로우 관계를 user_follows 테이블에 추가합니다.
     * @param followerUserNo 팔로우를 한 사람의 userNo
     * @param followedUserNo 팔로우를 당한 사람의 userNo
     */
    void follow(@Param("followerUserNo") String followerUserNo, @Param("followedUserNo") String followedUserNo);

    /**
     * 기존 팔로우 관계를 user_follows 테이블에서 삭제합니다.
     * @param followerUserNo 팔로우를 한 사람의 userNo
     * @param followedUserNo 팔로우를 당한 사람의 userNo
     */
    void unfollow(@Param("followerUserNo") String followerUserNo, @Param("followedUserNo") String followedUserNo);
}

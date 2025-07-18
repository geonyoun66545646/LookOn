package ks55team02.customer.follow.service;

public interface FollowService {

	/**
     * 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인합니다.
     * @param followerUserNo 현재 로그인한 사용자 (팔로우 하는 사람)
     * @param followedUserNo 대상 사용자 (팔로우 당하는 사람)
     * @return 팔로우 중이면 true, 아니면 false
     */
    boolean isFollowing(String followerUserNo, String followedUserNo);

    /**
     * 팔로우를 실행합니다.
     * @param followerUserNo 현재 로그인한 사용자
     * @param followedUserNo 대상 사용자
     */
    void follow(String followerUserNo, String followedUserNo);

    /**
     * 언팔로우를 실행합니다.
     * @param followerUserNo 현재 로그인한 사용자
     * @param followedUserNo 대상 사용자
     */
    void unfollow(String followerUserNo, String followedUserNo);
}

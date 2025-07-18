/**
 * =======================================================================
 * 팔로우/언팔로우 기능 전용 스크립트 (follow.js)
 * =======================================================================
 */

// 전역 스코프에 변수를 추가하여, 언팔로우할 대상의 정보를 임시 저장합니다.
let userToUnfollow = {
    targetArea: null,
    writerUserNo: null
};

/**
 * [메인 함수] 팔로우 버튼의 초기 상태를 결정하고 화면에 표시합니다.
 */
function initializeFollowButton(container, writerUserNo, loginUserNo) {
    const $followArea = $(container).find('.follow-action-area');
    if (!loginUserNo || loginUserNo === writerUserNo || !writerUserNo) {
        $followArea.empty();
        return;
    }
    $.ajax({
        type: 'GET',
        url: `/api/v1/follows/status?targetUserNo=${writerUserNo}`,
    })
    .done(function(response) {
        if (response.isFollowing) {
            renderFollowingButton($followArea, writerUserNo);
        } else {
            renderFollowButton($followArea, writerUserNo);
        }
    })
    .fail(function(xhr) { console.error("팔로우 상태 확인 실패:", xhr); });
}

/**
 * [렌더링 함수] '팔로우' 버튼 HTML을 생성하고 이벤트를 연결합니다.
 */
function renderFollowButton($targetArea, writerUserNo) {
    const followButtonHtml = `<a href="#" class="follow-link">팔로우</a>`; // · 제거
    $targetArea.html(followButtonHtml);
    $targetArea.find('.follow-link').on('click', function(e) {
        e.preventDefault();
        handleFollowClick($targetArea, writerUserNo);
    });
}

/**
 * [렌더링 함수] '팔로잉' 버튼 HTML을 생성하고 이벤트를 연결합니다.
 */
function renderFollowingButton($targetArea, writerUserNo) {
    const followingButtonHtml = `<a href="#" class="following-link">팔로잉</a>`; // · 제거
    $targetArea.html(followingButtonHtml);
    $targetArea.find('.following-link').on('click', function(e) {
        e.preventDefault();
        handleFollowingClick($targetArea, writerUserNo);
    });
}

/**
 * [이벤트 핸들러] '팔로우' 버튼 클릭 시 실행됩니다.
 */
function handleFollowClick($targetArea, writerUserNo) {
    $.ajax({
        type: 'POST',
        url: '/api/v1/follows',
        contentType: 'application/json',
        data: JSON.stringify({ "followedUserNo": writerUserNo })
    })
    .done(function() {
        renderFollowingButton($targetArea, writerUserNo);
    })
    .fail(function(xhr) {
        console.error("팔로우 실행 실패:", xhr);
        alert("팔로우 요청에 실패했습니다.");
    });
}

/**
 * [이벤트 핸들러] '팔로잉' 버튼 클릭 시 실행됩니다.
 * 언팔로우 확인 모달을 띄우는 역할을 합니다.
 */
function handleFollowingClick($targetArea, writerUserNo) {
    // ▼▼▼ [수정] confirm 대신 모달을 띄우도록 변경 ▼▼▼
    
    // 1. 모달에 표시할 상대방의 프로필 이미지와 아이디를 가져옵니다.
    const $userProfile = $targetArea.closest('.user-profile');
    const profileImgSrc = $userProfile.find('.profile-image').attr('src');
    const username = $userProfile.find('.username').text();

    // 2. 모달의 내용을 채웁니다.
    $('#unfollow-modal-img').attr('src', profileImgSrc);
    $('#unfollow-modal-text').html(`<strong>@${username}</strong>님의 팔로우를 취소하시겠어요?`);
    
    // 3. 나중에 '팔로우 취소' 버튼을 눌렀을 때, 누구를 언팔로우할지 알 수 있도록 정보를 저장해둡니다.
    userToUnfollow = {
        targetArea: $targetArea,
        writerUserNo: writerUserNo
    };

    // 4. 모달을 띄웁니다.
    $('#unfollow-confirm-modal').modal('show');
}

// ▼▼▼ [신규 추가] 모달의 '팔로우 취소' 버튼 클릭 이벤트를 페이지 로딩 시 한 번만 연결합니다. ▼▼▼
$(document).ready(function() {
    $('#unfollow-confirm-btn').on('click', function() {
        // 이전에 저장해 둔 정보를 가져옵니다.
        const { targetArea, writerUserNo } = userToUnfollow;

        // 정보가 없으면 아무것도 하지 않습니다. (안전 장치)
        if (!targetArea || !writerUserNo) return;

        // 서버에 언팔로우 요청을 보냅니다. (API 3 호출)
        $.ajax({
            type: 'DELETE',
            url: '/api/v1/follows',
            contentType: 'application/json',
            data: JSON.stringify({ "followedUserNo": writerUserNo })
        })
        .done(function() {
            // 성공 시, 버튼을 다시 '팔로우' 상태로 되돌립니다.
            renderFollowButton(targetArea, writerUserNo);
        })
        .fail(function(xhr) {
            console.error("언팔로우 실행 실패:", xhr);
            alert("언팔로우 요청에 실패했습니다.");
        })
        .always(function() {
            // 성공하든 실패하든, 모달을 닫습니다.
            $('#unfollow-confirm-modal').modal('hide');
        });
    });
});
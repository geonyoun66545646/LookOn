/**
 * 이 파일은 피드 상세 페이지와 사용자별 피드 모음 페이지 모두에서
 * '...' 옵션 버튼과 관련된 모든 기능을 전담합니다.
 * 두 페이지의 HTML 구조가 다르므로, 이를 모두 처리할 수 있도록 코드가 작성되었습니다.
 */
$(() => {
    // --- 1. 전역 변수 및 초기 설정 ---

    // [수정] 두 페이지를 모두 포함할 수 있는 범용적인 컨테이너를 찾습니다.
    // feedDetail.html은 #feed-container, feedListByUserNo.html은 .my-feed-container 입니다.
    const $container = $('#feed-container, .my-feed-container');

    // [수정] 페이지에 컨테이너가 없으면, 이 스크립트는 아무 작업도 하지 않고 조용히 종료됩니다.
    if ($container.length === 0) {
        return;
    }

    // [수정] 로그인 사용자 번호는 data 속성에서 가져옵니다. 두 HTML 모두 동일한 속성명을 사용합니다.
    const loginUserNo = $container.data('login-user-no');

    // 모달 클릭 시 사용할 정보를 임시 저장하는 변수 (피드 정보 또는 사용자 정보)
    let currentTargetInfo = {
        type: null, // 'feed' 또는 'user'
        feedSn: null,
        writerUserNo: null,
        pageOwnerUserNo: null // 사용자 프로필 페이지에서만 사용
    };

    // --- 2. 함수 정의 ---

    /**
     * [수정] 피드 옵션 모달을 열고, 상황에 맞는 메뉴를 구성합니다.
     * 이제 이 함수는 '피드'에 대한 옵션만을 담당합니다.
     */
    function openFeedOptionsMenu(writerUserNo) {
        const $optionsList = $('#feed-options-list');
        const $modal = $('#feed-options-modal');

        $optionsList.html('<li class="list-group-item text-center">불러오는 중...</li>');
        $modal.modal('show');

        const isMyPost = loginUserNo && loginUserNo === writerUserNo;

        if (isMyPost) {
            // 내 게시물 메뉴
            const menuItemsHtml = `
                <li class="list-group-item" data-action="edit-feed" style="cursor: pointer;"><b>수정</b></li>
                <li class="list-group-item text-danger" data-action="delete-feed" style="cursor: pointer;"><b>삭제</b></li>
                <li class="list-group-item" data-dismiss="modal" style="cursor: pointer;">취소</li>
            `;
            $optionsList.html(menuItemsHtml);
            return;
        }

        $.ajax({
            type: 'GET',
            url: '/api/v1/follows/status',
            data: { targetUserNo: writerUserNo },
            dataType: 'json'
        })
        .done(function(response) {
            // 다른 사람 게시물 메뉴
            let menuItemsHtml = '';
            if (response.isFollowing) {
                menuItemsHtml += `<li class="list-group-item text-danger" data-action="unfollow" style="cursor: pointer;"><b>팔로우 취소</b></li>`;
            }
            menuItemsHtml += `<li class="list-group-item text-danger" data-action="report-feed" style="cursor: pointer;"><b>신고</b></li>`;
            menuItemsHtml += `<li class="list-group-item" data-action="goto-feed" style="cursor: pointer;">피드 가기</li>`;
            menuItemsHtml += `<li class="list-group-item" data-dismiss="modal" style="cursor: pointer;">취소</li>`;
            $optionsList.html(menuItemsHtml);
        })
        .fail(() => {
            $optionsList.html('<li class="list-group-item">메뉴를 불러오지 못했습니다.</li><li class="list-group-item" data-dismiss="modal">닫기</li>');
        });
    }
    
    /**
     * [신규 추가] 사용자 프로필 옵션 모달을 여는 함수
     */
    function openUserOptionsMenu() {
        const $optionsList = $('#feed-options-list');
        const menuItemsHtml = `
            <li class="list-group-item text-danger" data-action="report-user" style="cursor: pointer;"><b>이용자 신고</b></li>
            <li class="list-group-item" data-dismiss="modal" style="cursor: pointer;">취소</li>
        `;
        $optionsList.html(menuItemsHtml);
        $('#feed-options-modal').modal('show');
    }

    // --- 3. 이벤트 핸들러 ---

    // 3-1. '...' 버튼 클릭 이벤트 통합
    $container.on('click', '.options-menu-btn', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const $button = $(this);
        const $feedWrapper = $button.closest('.feed-post-wrapper');
        const $profileActions = $button.closest('.profile-actions');

        if ($feedWrapper.length > 0) {
            // 클릭된 버튼이 '피드' 내부에 있는 경우
            currentTargetInfo = {
                type: 'feed',
                feedSn: $feedWrapper.data('feed-sn'),
                writerUserNo: $feedWrapper.data('wrtr-user-no')
            };
            openFeedOptionsMenu(currentTargetInfo.writerUserNo);

        } else if ($profileActions.length > 0) {
            // 클릭된 버튼이 '사용자 프로필' 영역에 있는 경우
            currentTargetInfo = {
                type: 'user',
                pageOwnerUserNo: $container.data('page-owner-user-no')
            };
            openUserOptionsMenu();
        }
    });

    // 3-2. 모달 내 메뉴 클릭 이벤트 통합
    $('#feed-options-list').on('click', 'li[data-action]', function() {
        const action = $(this).data('action');

        $('#feed-options-modal').modal('hide');

        switch (action) {
            // 피드 관련 액션
            case 'edit-feed':
                window.location.href = `/customer/feed/edit/${currentTargetInfo.feedSn}`;
                break;
            case 'delete-feed':
                if (confirm('정말로 이 피드를 삭제하시겠습니까?')) {
                    $.ajax({
                        url: `/customer/api/feeds/${currentTargetInfo.feedSn}`, type: 'DELETE'
                    }).done(() => {
                        alert('피드가 삭제되었습니다.');
                        window.location.href = '/customer/feed/feedList';
                    }).fail(() => alert('삭제에 실패했습니다.'));
                }
                break;
            case 'report-feed':
                window.location.href = `/customer/reports?feedSn=${currentTargetInfo.feedSn}`;
                break;
            case 'goto-feed':
                window.location.href = `/customer/feed/feedListByUserNo/${currentTargetInfo.writerUserNo}`;
                break;
            case 'unfollow':
                 // 언팔로우 로직 (기존과 동일)
                const $header = $(`.feed-post-wrapper[data-wrtr-user-no="${currentTargetInfo.writerUserNo}"]`).find('.feed-post-header');
                const $followArea = $header.find('.follow-action-area');
                // follow.js의 handleFollowingClick 함수를 간접적으로 호출
                if ($followArea.find('.following-btn').length > 0) {
                     $followArea.find('.following-btn').trigger('click');
                }
                break;

            // 사용자 관련 액션
            case 'report-user':
                window.location.href = `/customer/reports?userNo=${currentTargetInfo.pageOwnerUserNo}`;
                break;
        }
    });

    // [신규 추가] '사용자별 피드 모음' 페이지의 팔로우 버튼 초기화
    if ($container.hasClass('my-feed-container')) {
        const pageOwnerUserNo = $container.data('page-owner-user-no');
        if (loginUserNo && pageOwnerUserNo && loginUserNo !== pageOwnerUserNo) {
            if (typeof initializeFollowButton === 'function') {
                const actionArea = $('.profile-actions .other-user-actions');
                initializeFollowButton(actionArea, pageOwnerUserNo, loginUserNo);
            }
        }
    }
});
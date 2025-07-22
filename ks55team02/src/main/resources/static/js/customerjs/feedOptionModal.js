/**
 * 이 파일은 피드 상세 페이지와 사용자별 피드 모음 페이지 모두에서
 * '...' 옵션 버튼과 관련된 모든 기능을 전담합니다.
 */
$(() => {
    // --- 1. 전역 변수 및 초기 설정 ---
    const $container = $('#feed-container, .my-feed-container');
    if ($container.length === 0) {
        return;
    }
    const loginUserNo = $container.data('login-user-no');
    let currentTargetInfo = {
        type: null,
        feedSn: null,
        writerUserNo: null,
        pageOwnerUserNo: null
    };

    // --- 2. 함수 정의 ---

    /**
     * [최종 수정] 피드 옵션 모달을 열고, 로직 순서를 바로잡은 버전
     */
    function openFeedOptionsMenu(writerUserNo) {
        const $optionsList = $('#feed-options-list');
        const $modal = $('#feed-options-modal');

        const isMyPost = loginUserNo && String(loginUserNo) === String(writerUserNo);

        if (isMyPost) {
            // 내 게시물인 경우: 바로 메뉴를 구성하고 모달을 띄웁니다.
            const menuItemsHtml = `
                <li class="list-group-item" data-action="edit-feed" style="cursor: pointer;"><b>수정</b></li>
                <li class="list-group-item text-danger" data-action="delete-feed" style="cursor: pointer;"><b>삭제</b></li>
                <li class="list-group-item" data-dismiss="modal" style="cursor: pointer;">취소</li>
            `;
            $optionsList.html(menuItemsHtml);
            $modal.modal('show');
            return;
        }

        // ▼▼▼ [핵심 수정] 다른 사람 게시물일 경우, 모든 동작 전에 로그인 여부부터 확인합니다. ▼▼▼
        if (!loginUserNo) {
            // 비로그인 상태이면, 다른 모달을 띄울 필요 없이 바로 로그인 모달만 띄웁니다.
            $('#signin-modal').modal('show');
            return; // 여기서 함수를 완전히 종료합니다.
        }
        // ▲▲▲ [핵심 수정] ▲▲▲

        // 이제, 로그인 상태임이 보장되었으므로 "불러오는 중..."을 띄우고 Ajax 요청을 시작합니다.
        $optionsList.html('<li class="list-group-item text-center">불러오는 중...</li>');
        $modal.modal('show');

        $.ajax({
            type: 'GET',
            url: '/api/v1/follows/status',
            data: { targetUserNo: writerUserNo },
            dataType: 'json'
        })
        .done(function(response) {
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
    $container.on('click', '.options-menu-btn', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const $button = $(this);
        const $feedWrapper = $button.closest('.feed-post-wrapper');
        const $profileActions = $button.closest('.profile-actions');

        if ($feedWrapper.length > 0) {
            currentTargetInfo = {
                type: 'feed',
                feedSn: $feedWrapper.data('feed-sn'),
                writerUserNo: $feedWrapper.data('wrtr-user-no')
            };
            openFeedOptionsMenu(currentTargetInfo.writerUserNo);
        } else if ($profileActions.length > 0) {
            currentTargetInfo = {
                type: 'user',
                pageOwnerUserNo: $container.data('page-owner-user-no')
            };
            openUserOptionsMenu();
        }
    });

    $('#feed-options-list').on('click', 'li[data-action]', function() {
        const action = $(this).data('action');
        $('#feed-options-modal').modal('hide');

        switch (action) {
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
                const $header = $(`.feed-post-wrapper[data-wrtr-user-no="${currentTargetInfo.writerUserNo}"]`).find('.feed-post-header');
                const $followArea = $header.find('.follow-action-area');
                if ($followArea.find('.following-link').length > 0) {
                     $followArea.find('.following-link').trigger('click');
                }
                break;
            case 'report-user':
                window.location.href = `/customer/reports?userNo=${currentTargetInfo.pageOwnerUserNo}`;
                break;
        }
    });

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
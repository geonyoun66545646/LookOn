$(() => {
    let currentPage = 1;
    let isLoading = false;
    // 컨트롤러에서 전달받은 hasNext 값을 JS 변수로 초기화
    let hasNext = /*[[${hasNext}]]*/ true; 

    // 더 이상 로드할 페이지가 없으면 스크롤 이벤트 자체를 등록하지 않음
    if (!hasNext) {
        return;
    }

    const loadNextPage = () => {
        if (isLoading || !hasNext) {
            return; // 이미 로딩 중이거나 다음 페이지가 없으면 중단
        }

        isLoading = true;
        currentPage++;
        $('#loading-indicator').show(); // 로딩 인디케이터 표시

        $.ajax({
            url: '/customer/feed/myFeed',
            type: 'GET',
            data: {
                page: currentPage
            }
        })
        .done(responseHtml => {
            // responseHtml이 비어있으면 더 이상 데이터가 없는 것으로 간주
            
            // 받아온 HTML 조각(자식 요소들)을 기존 그리드에 추가
            const newItems = $(responseHtml).find('.feed-item');
            if (newItems.length > 0) {
                $('.feed-grid').append(newItems);
            } else {
                // 반환된 아이템이 없으면 마지막 페이지로 간주
                hasNext = false;
                $(window).off('scroll', onScroll); // 스크롤 이벤트 제거
            }
        })
        .fail(error => {
            console.error('피드를 불러오는데 실패했습니다.', error);
        })
        .always(() => {
            isLoading = false;
            $('#loading-indicator').hide(); // 로딩 인디케이터 숨김
        });
    };

    const onScroll = () => {
        // (전체 문서 높이 - 윈도우 높이) == 현재 스크롤 위치
        // 거의 끝에 도달했을 때 (1px 여유) 로드하도록 함
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 1) {
            loadNextPage();
        }
    };

    // 스크롤 이벤트 리스너 등록
    $(window).on('scroll', onScroll);
});
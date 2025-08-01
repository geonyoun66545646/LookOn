// inquiryDetail.js

document.addEventListener('DOMContentLoaded', function() {
    // 필요한 DOM 요소들을 찾음
    const editBtn = document.getElementById('editBtn');
    const deleteBtn = document.getElementById('deleteBtn');
    const saveEditBtn = document.getElementById('saveEditBtn');
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    
    const viewMode = document.getElementById('viewMode');
    const editMode = document.getElementById('editMode');
    const editContent = document.getElementById('editContent');

    // ✅ [추가] 버튼 그룹을 제어하기 위한 요소
    const viewModeButtons = document.getElementById('viewModeButtons');
    const editModeButtons = document.getElementById('editModeButtons');

    // '수정하기' 버튼 클릭 이벤트
    if (editBtn) {
        editBtn.addEventListener('click', function() {
            // 내용 <-> 입력창 전환
            viewMode.style.display = 'none';
            editMode.style.display = 'block';
            
            // ✅ [수정] '수정/삭제' 버튼 그룹을 숨기고 '저장/취소' 그룹을 보여줌
            if (viewModeButtons) viewModeButtons.style.display = 'none';
            if (editModeButtons) editModeButtons.style.display = 'inline-block';
        });
    }

    // '취소' 버튼 클릭 이벤트
    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', function() {
            // 내용 <-> 입력창 전환
            viewMode.style.display = 'block';
            editMode.style.display = 'none';
            
            // ✅ [수정] '저장/취소' 버튼 그룹을 숨기고 '수정/삭제' 그룹을 보여줌
            if (editModeButtons) editModeButtons.style.display = 'none';
            if (viewModeButtons) viewModeButtons.style.display = 'inline-block';
        });
    }

    // '저장' 버튼 클릭 이벤트 (AJAX)
    if (saveEditBtn) {
        saveEditBtn.addEventListener('click', function() {
            const inquiryDataContainer = document.getElementById('inquiryDataContainer');
            if (!inquiryDataContainer || !inquiryDataContainer.dataset.inquiry) {
                alert('문의 데이터를 찾을 수 없습니다.');
                return;
            }

            // data 속성에서 원본 데이터를 가져와 내용만 교체
            const updatedData = JSON.parse(inquiryDataContainer.dataset.inquiry);
            updatedData.inqryCn = editContent.value;

            fetch('/customer/inquiry/updateInquiryAjax', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updatedData)
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { throw new Error(err.message || '서버 응답 오류'); });
                }
                return response.json();
            })
            .then(data => {
                alert(data.message);
                // 화면 내용 업데이트
                document.querySelector('#viewMode .inquiry-content').innerHTML = data.updatedContentHtml;
                
                // ✅ [수정] 수정된 내용을 data-* 속성에도 반영 (선택사항이지만 좋은 습관입니다)
                updatedData.inqryCn = data.updatedContentText; // 서버에서 받은 최종 텍스트로 업데이트
                inquiryDataContainer.dataset.inquiry = JSON.stringify(updatedData);

                // 모드 전환을 위해 '취소' 버튼 클릭 로직을 재사용
                cancelEditBtn.click();
            })
            .catch(error => {
                console.error('Error:', error);
                alert('오류: ' + error.message);
            });
        });
    }

    // '삭제하기' 버튼 클릭 이벤트 (기존과 동일)
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function() {
            if (confirm('정말로 이 문의를 삭제하시겠습니까?')) {
                const inquiryId = this.dataset.inquiryId;
                
                fetch('/customer/inquiry/deleteInquiryAjax', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ inquiryId: inquiryId })
                })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(err => { throw new Error(err.message || '서버 응답 오류'); });
                    }
                    return response.json();
                })
                .then(data => {
                    alert(data.message);
                    if (data.redirectUrl) {
                        window.location.href = data.redirectUrl;
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('오류: ' + error.message);
                });
            }
        });
    }
});
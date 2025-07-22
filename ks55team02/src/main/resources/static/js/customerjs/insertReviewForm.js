document.addEventListener('DOMContentLoaded', function() {
    // 폼 유효성 검사 (부트스트랩 기본 유효성 검사 활용)
    var forms = document.querySelectorAll('.needs-validation');
    Array.prototype.slice.call(forms)
        .forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        });

    // --- 별점 시스템 ---
    const ratingStars = document.querySelector('.rating-stars');
    const hiddenRatingInput = document.getElementById('rating');
    const starIcons = ratingStars.querySelectorAll('.star-icon');

    // 별 클릭 시 평점 설정
    ratingStars.addEventListener('click', function(e) {
        if (e.target.classList.contains('star-icon')) {
            const value = parseInt(e.target.dataset.value);
            hiddenRatingInput.value = value;
            updateStars(value);
            // 유효성 피드백 제거
            ratingStars.classList.remove('is-invalid');
            ratingStars.closest('.form-group').querySelector('.invalid-feedback').style.display = 'none';
        }
    });

    // 마우스 오버 시 별 색상 변경
    ratingStars.addEventListener('mouseover', function(e) {
        if (e.target.classList.contains('star-icon')) {
            const value = parseInt(e.target.dataset.value);
            updateStars(value, true); // Hover 상태로 업데이트
        }
    });

    // 마우스 아웃 시 원래 평점으로 돌아옴
    ratingStars.addEventListener('mouseout', function() {
        const selectedValue = parseInt(hiddenRatingInput.value || '0');
        updateStars(selectedValue);
    });

    function updateStars(selectedValue, isHover = false) {
        starIcons.forEach(star => {
            const starValue = parseInt(star.dataset.value);
            if (starValue <= selectedValue) {
                star.classList.remove('far');
                star.classList.add('fas'); // 채워진 별
            } else {
                star.classList.remove('fas');
                star.classList.add('far'); // 빈 별
            }
        });
    }

    // 초기 로드 시 평점 업데이트 (기존 값이 있다면)
    if (hiddenRatingInput.value) {
        updateStars(parseInt(hiddenRatingInput.value));
    }


    // --- 이미지 미리보기 및 파일 선택 레이블 업데이트 ---
    const reviewImagesInput = document.getElementById('review_images');
    const customFileLabel = reviewImagesInput.nextElementSibling;
    const imagePreviewContainer = document.getElementById('imagePreview');
    const maxFiles = 5; // 최대 파일 개수

    reviewImagesInput.addEventListener('change', function() {
        const files = this.files;
        let fileNames = [];
        imagePreviewContainer.innerHTML = ''; // 기존 미리보기 초기화

        if (files.length > maxFiles) {
            alert(`이미지는 최대 ${maxFiles}개까지만 첨부할 수 있습니다.`);
            this.value = ''; // 파일 선택 초기화
            customFileLabel.textContent = '파일 선택...';
            return;
        }

        if (files.length > 0) {
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                fileNames.push(file.name);

                // 이미지 미리보기 생성
                const reader = new FileReader();
                reader.onload = (e) => {
                    const imgItem = document.createElement('div');
                    imgItem.classList.add('image-item', 'mr-2', 'mb-2');
                    imgItem.innerHTML = `
                        <img src="${e.target.result}" alt="Review Image Preview">
                        <span class="remove-image" data-index="${i}">&times;</span>
                    `;
                    imagePreviewContainer.appendChild(imgItem);

                    // 미리보기 이미지 삭제 기능 (동적으로 추가된 요소에 이벤트 리스너)
                    imgItem.querySelector('.remove-image').addEventListener('click', function() {
                        const indexToRemove = parseInt(this.dataset.index);
                        removeFileFromFileList(reviewImagesInput, indexToRemove);
                        // UI 업데이트를 위해 change 이벤트를 다시 발생시킴
                        reviewImagesInput.dispatchEvent(new Event('change'));
                    });
                };
                reader.readAsDataURL(file);
            }
            customFileLabel.textContent = fileNames.join(', ');
        } else {
            customFileLabel.textContent = '파일 선택...';
        }
    });

    // 선택된 파일 목록에서 특정 인덱스의 파일 제거
    function removeFileFromFileList(input, index) {
        const dt = new DataTransfer();
        const files = input.files;

        for (let i = 0; i < files.length; i++) {
            if (index !== i) {
                dt.items.add(files[i]);
            }
        }
        input.files = dt.files; // DataTransfer 객체를 통해 파일 목록 업데이트
    }
});
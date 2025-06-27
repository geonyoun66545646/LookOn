$(document).ready(function() {
    // Sidebar Navigation Logic
    $('#productRegMenu .nav-link').on('click', function(e) {
        e.preventDefault();
        $('#productRegMenu .nav-link').removeClass('active');
        $(this).addClass('active');

        var targetSectionId = $(this).data('section');
        $('.content-section').removeClass('active');
        $('#' + targetSectionId).addClass('active');

        // Scroll to top of content area when section changes
        // $('.content-area').scrollTop(0);
    });

    // Initial section activation
    $('.content-section:first').addClass('active');
    $('#productRegMenu .nav-link:first').addClass('active');

    // --- 카테고리 2차 동적 로드 (AJAX 기반으로 수정) ---
    $('#productCategory1').on('change', function() {
        var selectedPrimaryCategoryId = $(this).val();
        var $productCategory2 = $('#productCategory2');
        var $productCategory2Group = $('#productCategory2Group');

        $productCategory2.empty();
        $productCategory2.append('<option value="">2차 카테고리 선택</option>');

        if (selectedPrimaryCategoryId) {
            $.ajax({
                url: '/seller/products/subcategories/' + selectedPrimaryCategoryId,
                type: 'GET',
                success: function(subProductCategories) {
                    if (subProductCategories && subProductCategories.length > 0) {
                        $.each(subProductCategories, function(i, pCategory) {
                            $productCategory2.append('<option value="' + pCategory.categoryId + '">' + pCategory.categoryName + '</option>');
                        });
                        $productCategory2Group.show();
                    } else {
                        $productCategory2Group.hide();
                    }
                },
                error: function(xhr, status, error) {
                    console.error("2차 카테고리 로드 중 오류 발생:", error);
                    $productCategory2Group.hide();
                }
            });
        } else {
            $productCategory2Group.hide();
        }
    }).trigger('change');

    // --- 가격 입력 시 원화 포맷팅 및 할인율/할인가 계산 ---
    function formatCurrency(number) {
        if (isNaN(number) || number === null) return ''; // 숫자가 아니거나 null이면 빈 문자열 반환
        return new Intl.NumberFormat('ko-KR').format(number);
    }

    function parseCurrency(str) {
        if (typeof str !== 'string' || str.trim() === '') return 0; // 문자열이 아니거나 비어있으면 0 반환
        return parseInt(str.replace(/[^0-9]/g, '')) || 0; // 숫자와 쉼표를 제외한 모든 문자 제거 후 파싱
    }

    $('#basePrice').on('input', function() {
        var value = parseCurrency($(this).val());
        $(this).val(formatCurrency(value));
        calculateFinalPrice();
    });

    $('#discountRate').on('input', function() {
        calculateFinalPrice();
    });

    // 재고 수량 입력 시에도 쉼표 제거 및 숫자만 입력되도록 처리
    $('#stockQuantity').on('input', function() {
        var value = parseCurrency($(this).val());
        $(this).val(formatCurrency(value)); // 재고도 쉼표 포맷팅을 원한다면 유지, 아니라면 제거
    });

    // 최대 구매 수량 입력 시에도 쉼표 제거 및 숫자만 입력되도록 처리 (옵션)
    $('#maxPurchase').on('input', function() {
        var value = parseCurrency($(this).val());
        $(this).val(formatCurrency(value)); // 최대 구매 수량도 쉼표 포맷팅을 원한다면 유지, 아니라면 제거
    });


    function calculateFinalPrice() {
        var basePrice = parseCurrency($('#basePrice').val());
        var discountRate = parseFloat($('#discountRate').val()) || 0;
        var finalPrice = basePrice * (1 - (discountRate / 100));
        $('#finalPrice').val(formatCurrency(Math.round(finalPrice)));
    }
    calculateFinalPrice();

    // --- 배송비 설정 토글 ---
    $('#shippingType').on('change', function() {
        if ($(this).val() === 'fixed' || $(this).val() === 'conditional') {
            $('#shippingFeeGroup').show();
            $('#shippingFee').prop('required', true);
        } else {
            $('#shippingFeeGroup').hide();
            $('#shippingFee').prop('required', false);
        }
    }).trigger('change');

    // 성별 옵션 버튼 클릭 이벤트
    $(document).on('click', '.option-gender-btn', function() {
        $('.option-gender-btn').removeClass('active');
        $(this).addClass('active');
        $('#genderOption').val($(this).data('value')).trigger('input'); // hidden input 값 설정 및 트리거
        updateOptionCombinations();
    });

    // 색상 옵션 데이터
    var colors = ['블랙', '화이트', '레드', '블루', '그린', '옐로우', '핑크', '그레이', '네이비', '브라운', '베이지', '오렌지', '퍼플', '실버', '기타 패턴', '기타 색상'];

    // 사이즈 옵션 데이터
    var sizes = {
        size_apparel: ['XS', 'S', 'M', 'L', 'XL', '2XL', '3XL'],
        size_fashion_acc: ['FREE'],
        size_shoes: Array.from({length: (300 - 210) / 5 + 1}, (_, i) => 210 + (i * 5)).map(String)
    };

    // 색상 옵션 동적 생성
    function populateColorOptions() {
        var $colorOptionsContainer = $('#colorOptionValues');
        $colorOptionsContainer.empty();
        colors.forEach(function(color) {
            $colorOptionsContainer.append(`<button type="button" class="btn btn-outline-secondary option-value-btn" data-type="color" data-value="${color}">${color}</button>`);
        });
    }

    // 사이즈 유형 선택 시 사이즈 옵션 동적 생성
    $('#sizeOptionType').on('change', function() {
        var selectedType = $(this).val();
        var $sizeOptionsContainer = $('#sizeOptionValues');
        $sizeOptionsContainer.empty();

        if (selectedType !== 'none' && sizes[selectedType]) {
            sizes[selectedType].forEach(function(size) {
                $sizeOptionsContainer.append(`<button type="button" class="btn btn-outline-secondary option-value-btn" data-type="size" data-value="${size}">${size}</button>`);
            });
        } else {
            $sizeOptionsContainer.append('<p class="text-muted">사이즈 유형을 선택하면 값이 표시됩니다.</p>');
        }
        updateOptionCombinations();
    });

    // 옵션값 버튼 클릭 시 활성/비활성 토글
    $(document).on('click', '.option-value-btn', function() {
        $(this).toggleClass('active');
        updateOptionCombinations();
    });

    // 상품명 변경 시 옵션 조합 업데이트 트리거
    $('#optionProductName').on('input', function() {
        updateOptionCombinations();
    });

    // --- 옵션 조합 생성 및 업데이트 함수 ---
    function updateOptionCombinations() {
        var productName = $('#optionProductName').val().trim();
        var selectedGender = $('#genderOption').val();

        var selectedColors = [];
        $('#colorOptionValues .option-value-btn.active').each(function() {
            selectedColors.push($(this).data('value'));
        });

        var selectedSizes = [];
        $('#sizeOptionValues .option-value-btn.active').each(function() {
            selectedSizes.push($(this).data('value'));
        });

        // --- 여기부터 DTO 바인딩을 위한 숨겨진 input 필드 생성/관리 ---

        // 기존에 생성된 숨겨진 옵션 필드들을 제거합니다. (중복 방지)
        // 폼 제출 시마다 새로운 조합으로 업데이트 되므로, 이전 값을 지우고 새로 추가
        $('#productRegistrationForm input[name="colorOptions"]').remove();
        $('#productRegistrationForm input[name="sizeOptions"]').remove();
        $('#productRegistrationForm input[name="optionCombinationNames"]').remove();
        $('#productRegistrationForm input[name="optionCombinationStocks"]').remove();


        // 선택된 색상 옵션을 숨겨진 input 필드로 추가
        selectedColors.forEach(function(color) {
            $('<input>').attr({
                type: 'hidden',
                name: 'colorOptions',
                value: color
            }).appendTo('#productRegistrationForm');
        });

        // 선택된 사이즈 옵션을 숨겨진 input 필드로 추가
        selectedSizes.forEach(function(size) {
            $('<input>').attr({
                type: 'hidden',
                name: 'sizeOptions',
                value: size
            }).appendTo('#productRegistrationForm');
        });
        // --- 여기까지 DTO 바인딩을 위한 숨겨진 input 필드 생성/관리 ---


        var $combinationTableBody = $('#optionCombinationsTableBody');
        $combinationTableBody.empty();

        // 필수 입력 항목 유효성 검사 (조합 생성 전)
        if (!selectedGender && selectedColors.length === 0 && selectedSizes.length === 0) {
             $combinationTableBody.append('<tr><td colspan="2" class="text-center text-muted">성별, 색상, 사이즈 옵션을 선택하면 조합이 생성됩니다.</td></tr>');
            return;
        }
         if (!productName) {
            $combinationTableBody.append('<tr><td colspan="2" class="text-center text-muted">옵션 조합에 사용될 제품명을 입력해주세요.</td></tr>');
            return;
         }

        // 조합 테이블 생성을 위한 임시 배열
        var combinationsForTable = [];
        var tempColors = selectedColors.length === 0 ? [''] : selectedColors;
        var tempSizes = selectedSizes.length === 0 ? [''] : selectedSizes;
        var tempGender = selectedGender ? selectedGender : '';

        // 성별, 색상, 사이즈 조합 생성
        tempColors.forEach(function(color) {
            tempSizes.forEach(function(size) {
                let combinationName = productName;
                if (tempGender) {
                    combinationName += ' ' + tempGender;
                }
                if (color !== '') {
                    combinationName += ' ' + color;
                }
                if (size !== '') {
                    combinationName += ' ' + size;
                }
                combinationsForTable.push(combinationName.trim());
            });
        });

        // 중복 조합 제거 및 정렬
        combinationsForTable = [...new Set(combinationsForTable)];
        combinationsForTable.sort();

        // 옵션 조합별 재고 입력 필드 생성
        combinationsForTable.forEach(function(combination, index) {
            // 이전에 입력된 재고 값을 유지하기 위한 로직 추가 (필요시)
            // 현재는 새롭게 생성되므로 초기값은 빈칸 또는 0
            var rowHtml = `
                <tr>
                    <td>${combination}
                        <input type="hidden" name="optionCombinationNames" value="${combination}">
                    </td>
                    <td>
                        <input type="number" class="form-control form-control-sm" name="optionCombinationStocks" placeholder="수량" min="0" required>
                    </td>
                </tr>
            `;
            $combinationTableBody.append(rowHtml);
        });
    }

    // --- 이미지 업로드 미리보기 (상품 썸네일) ---
    function handleImageUpload(inputElement, previewContainerId, maxImages, isThumbnail = false) {
        var $previewContainer = $(previewContainerId);
        var $fileInputLabel = $(inputElement).next('.custom-file-label').find('.file-name');
        $previewContainer.empty();
        $previewContainer.removeClass('has-images');

        var files = inputElement.files;
        if (files.length === 0) {
            $fileInputLabel.text(isThumbnail ? '상품 썸네일 업로드 (1장)' : (maxImages === 15 ? '대표 이미지 업로드 (최소 1장, 최대 15장)' : '상세 페이지 이미지 업로드 (최소 1장, 최대 20장)'));
            $previewContainer.append('<p class="text-center">클릭 또는 파일을 여기에 끌어다 놓으세요.</p>');
            return;
        }

        $previewContainer.addClass('has-images');
        var filesToProcess = Array.from(files).slice(0, maxImages);

        // Update file input label
        var fileNames = Array.from(files).map(file => file.name).join(', ');
        $fileInputLabel.text(fileNames.length > 50 ? fileNames.substring(0, 50) + '...' : fileNames);


        filesToProcess.forEach((file, index) => {
            if (file.type.startsWith('image/')) {
                var reader = new FileReader();
                reader.onload = function(e) {
                    var imgClass = (previewContainerId === '#mainImagePreview' && index === 0) ? 'main-image' : '';
                    var imgHtml = `
                        <div class="image-upload-item ${imgClass}">
                            <img src="${e.target.result}" alt="${file.name}">
                            <button type="button" class="remove-btn" data-file-index="${index}" data-target-input="${inputElement.id}">x</button>
                        </div>
                    `;
                    $previewContainer.append(imgHtml);
                };
                reader.readAsDataURL(file);
            }
        });
    }
	
	// --- 이미지 업로드 change 이벤트 리스너 ---
    $('#thumbnailImageUpload').on('change', function() {
        handleImageUpload(this, '#thumbnailImagePreview', 1, true);
    });

    $('#mainImageUpload').on('change', function() {
        handleImageUpload(this, '#mainImagePreview', 15);
    });

    $('#detailImageUpload').on('change', function() {
        handleImageUpload(this, '#detailImagePreview', 20);
    });

    // Drag and drop functionality
    $('.image-upload-preview').on('dragover', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).css('border-color', '#c96');
    }).on('dragleave', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).css('border-color', '#ccc');
    }).on('drop', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).css('border-color', '#ccc');

        var files = e.originalEvent.dataTransfer.files;
        var inputId = $(this).prev('.custom-file').find('input[type="file"]').attr('id');
        var inputElement = document.getElementById(inputId);

        // Assign files to the input element
        inputElement.files = files;

        // Trigger change event manually to update preview
        $(inputElement).trigger('change');
    });


    // 이미지 미리보기 삭제 버튼 (공통)
    $(document).on('click', '.image-upload-item .remove-btn', function() {
        var $itemToRemove = $(this).closest('.image-upload-item');
        var inputId = $(this).data('target-input');
        var fileIndex = $(this).data('file-index');
        var inputElement = document.getElementById(inputId);
        var dataTransfer = new DataTransfer();
        var currentFiles = Array.from(inputElement.files);

        // Remove the file at the specified index
        currentFiles.splice(fileIndex, 1);

        // Add remaining files to DataTransfer object
        currentFiles.forEach(file => dataTransfer.items.add(file));

        // Update the input's files
        inputElement.files = dataTransfer.files;

        $itemToRemove.remove();

        // If no images left, show placeholder text
        var $previewContainer = $(inputElement).closest('.form-group').find('.image-upload-preview');
        if ($previewContainer.children('.image-upload-item').length === 0) {
            $previewContainer.removeClass('has-images').append('<p class="text-center">클릭 또는 파일을 여기에 끌어다 놓으세요.</p>');
        }

        // Update custom file input label
        var $fileInputLabel = $(inputElement).next('.custom-file-label').find('.file-name');
        if (inputElement.files.length > 0) {
            var fileNames = Array.from(inputElement.files).map(file => file.name).join(', ');
            $fileInputLabel.text(fileNames.length > 50 ? fileNames.substring(0, 50) + '...' : fileNames);
        } else {
            $fileInputLabel.text(
                (inputId === 'thumbnailImageUpload' ? '상품 썸네일 업로드 (1장)' :
                 (inputId === 'mainImageUpload' ? '대표 이미지 업로드 (최소 1장, 최대 15장)' :
                  '상세 페이지 이미지 업로드 (최소 1장, 최대 20장)'))
            );
        }
    });


    // WYSIWYG 에디터 초기화 (외부 라이브러리 필요)
    // if (typeof ClassicEditor !== 'undefined') { // 예: CKEditor 5
    //     ClassicEditor
    //         .create(document.querySelector('#productDescription'))
    //         .catch(error => {
    //             console.error(error);
    //         });
    // } else if (typeof tinymce !== 'undefined') { // 예: TinyMCE
    //     tinymce.init({
    //         selector: '#productDescription'
    //     });
    // }

    // 하단 버튼 액션 (예시)
    $('#previewBtn').on('click', function() {
        alert('상품 미리보기 기능 (구현 필요)');
    });
    $('#tempSaveBtn').on('click', function() {
        alert('상품 임시 저장 기능 (구현 필요)');
        // 폼 데이터를 AJAX로 전송하여 임시 저장
    });
    // '등록하기' 버튼은 form="productRegistrationForm" 속성으로 폼 제출

    // ⭐⭐ 수정된 취소 버튼 클릭 이벤트 ⭐⭐
    $('#cancelBtn').on('click', function() {
        if (confirm('작성 중인 내용을 취소하고 목록으로 돌아가시겠습니까?')) {
            // 컨트롤러의 취소 URL로 리다이렉션
            window.location.href = '/seller/products/cancelRegistration';
        }
    });

    // Character Counter Logic for styled inputs
    function setupCharCounter(inputId, counterId) {
        const $input = $(`#${inputId}`);
        const $counter = $(`#${counterId}`);
        const maxLength = parseInt($input.attr('maxlength'), 10);

        if ($input.length && $counter.length && !isNaN(maxLength)) {
            $input.on('input', function() {
                const currentLength = $(this).val().length;
                $counter.text(currentLength);
            });
            // Initial count
            $input.trigger('input');
        }
    }

    setupCharCounter('productName', 'productNameCounter');
    setupCharCounter('productDescription', 'productDescriptionCounter');
    // 'productBrand', 'productOrigin', 'certificationInfo'는 HTML에 input 필드가 없어서 주석 처리
    // setupCharCounter('productBrand', 'productBrandCounter');
    // setupCharCounter('productOrigin', 'productOriginCounter');
    // setupCharCounter('certificationInfo', 'certificationInfoCounter');
    setupCharCounter('videoUrl', 'videoUrlCounter');
    // 'deliveryPeriod', 'returnPolicy'는 HTML에 input 필드가 없어서 주석 처리
    // setupCharCounter('deliveryPeriod', 'deliveryPeriodCounter');
    // setupCharCounter('returnPolicy', 'returnPolicyCounter');
    setupCharCounter('productTags', 'productTagsCounter');
    setupCharCounter('optionProductName', 'optionProductNameCounter');

    // Initial calls to populate options and combinations if needed
    populateColorOptions();
    // 옵션 설정 항목들이 항상 노출되므로, 초기화 시 조합을 업데이트
    updateOptionCombinations();


    // --- 폼 제출 시 숫자 필드에서 쉼표 제거 및 유효성 검사 통합 ---
    $('#productRegistrationForm').on('submit', function(event) {
        // 모든 숫자 필드의 쉼표를 먼저 제거
        const basePriceInput = document.getElementById('basePrice');
        if (basePriceInput) {
            basePriceInput.value = basePriceInput.value.replace(/,/g, '');
        }
        const stockQuantityInput = document.getElementById('stockQuantity');
        if (stockQuantityInput) {
            stockQuantityInput.value = stockQuantityInput.value.replace(/,/g, '');
        }
        const maxPurchaseInput = document.getElementById('maxPurchase');
        if (maxPurchaseInput) {
            maxPurchaseInput.value = maxPurchaseInput.value.replace(/,/g, '');
        }
        $('input[name="optionCombinationStocks"]').each(function() {
            $(this).val($(this).val().replace(/,/g, ''));
        });

        // ⭐ 유효성 검사 시작 ⭐
        let isValid = true;
        let errorMessage = "다음 항목들을 입력해주세요:<br>";
        // let hasErrorInCurrentSection = false; // 이 변수는 현재 로직에서 굳이 필요하지 않아 제거

        // 현재 활성화된 섹션을 찾음
        const $activeSection = $('.content-section.active');
        
        // 1. 필수 입력 필드 검사 (현재 활성화된 섹션 내)
        $activeSection.find('input[required], select[required], textarea[required]').each(function() {
            const $input = $(this);
            const value = $input.val();
            
            // 파일 입력 필드인 경우 files 속성을 확인 (썸네일, 메인, 상세 이미지)
            if ($input.is('input[type="file"]')) {
                // thumbnailImageUpload의 경우 required지만 1장 이상이 없으면 안됨
                // mainImageUpload의 경우 required지만 1장 이상이 없으면 안됨
                // detailImageUpload의 경우 required인데, 파일이 없는 경우 체크
                if ($input.attr('id') === 'thumbnailImageUpload' || $input.attr('id') === 'mainImageUpload' || $input.attr('id') === 'detailImageUpload') {
                    if ($input[0].files.length === 0) {
                        let fieldName = $input.next('label').find('.file-name').text().replace(/\s*(\(\s*(최소)?\d+장,\s*(최대)?\d+장\))?\s*(\(\d+장\))?\s*\*\s*$/, '').trim();
                        // 파일명에서 "(최소 n장, 최대 n장)" 또는 "(n장)"과 "*" 제거
                        errorMessage += `- ${fieldName}<br>`;
                        isValid = false;
                    }
                }
            } else if (!value || (typeof value === 'string' && value.trim() === '')) {
                let fieldName = '';
                if ($input.attr('id')) {
                    const $label = $(`label[for="${$input.attr('id')}"]`);
                    if ($label.length > 0) {
                        fieldName = $label.text().replace(/\s*\*\s*$/, '').trim();
                    } else if ($input.closest('.form-group.styled-input-new').find('.label-segment').length > 0) {
                        fieldName = $input.closest('.form-group.styled-input-new').find('.label-segment').text().replace(/\s*\*\s*$/, '').trim();
                    }
                }
                if (!fieldName) {
                    fieldName = $input.attr('placeholder') || $input.attr('name') || '알 수 없는 필드';
                }
                errorMessage += `- ${fieldName}<br>`;
                isValid = false;
            }
        });

        // 2. 특정 조건부 필수 필드 검사 (예: 옵션 설정 섹션)
        if ($activeSection.attr('id') === 'optionSetting') {
            const $genderOption = $('#genderOption');
            const $selectedColors = $('#colorOptionValues .option-value-btn.active');
            const $selectedSizes = $('#sizeOptionValues .option-value-btn.active');
            const $optionProductName = $('#optionProductName');

            if ($optionProductName.val().trim() === '') {
                errorMessage += `- 옵션 조합 제품명<br>`;
                isValid = false;
            }
            // 성별, 색상, 사이즈 옵션은 필수이므로 선택 안하면 오류
            if ($genderOption.val() === '') {
                errorMessage += `- 성별 옵션<br>`;
                isValid = false;
            }
            if ($selectedColors.length === 0) {
                errorMessage += `- 색상 옵션<br>`;
                isValid = false;
            }
            if ($selectedSizes.length === 0) {
                errorMessage += `- 사이즈 옵션<br>`;
                isValid = false;
            }
            
            // 옵션 조합별 재고 수량 검사
            $('input[name="optionCombinationStocks"]').each(function() {
                if (!$(this).val() || parseInt($(this).val()) < 0) {
                    errorMessage += `- 옵션 조합 재고 수량<br>`;
                    isValid = false;
                    return false; // 첫 번째 빈 재고 발견 시 루프 중단
                }
            });
        }


        // 3. 최종 유효성 검사 결과 처리
        if (!isValid) {
            event.preventDefault(); // 폼 제출 중단
            Swal.fire({
                icon: "error",
                title: "입력 오류!",
                html: errorMessage + "<br>모든 필수 항목을 올바르게 채워주세요.", // html 사용 시 <br> 태그 적용
                confirmButtonText: "확인",
                customClass: {
                    container: 'my-swal-container',
                    popup: 'my-swal-popup',
                    header: 'my-swal-header',
                    title: 'my-swal-title',
                    content: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button'
                }
            });
            // 에러가 발생한 섹션으로 스크롤 이동 (선택 사항)
            // $('html, body').animate({
            //     scrollTop: $activeSection.offset().top - 100 // 상단 여백 조절
            // }, 500);

            return false; // 폼 제출 중단
        }

        // 유효성 검사를 모두 통과하면 폼 제출을 허용
        console.log("폼 제출: 모든 유효성 검사 통과");
        // 이 시점에서 폼은 정상적으로 제출됩니다.
    });

});
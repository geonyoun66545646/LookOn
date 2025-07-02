$(document).ready(function() {
    // Sidebar Navigation Logic
    $('#productRegMenu .nav-link').on('click', function(e) {
        e.preventDefault();
        $('#productRegMenu .nav-link').removeClass('active');
        $(this).addClass('active');

        var targetSectionId = $(this).data('section');
        $('.content-section').removeClass('active');
        $('#' + targetSectionId).addClass('active');
    });

    // Initial section activation
    $('.content-section:first').addClass('active');
    $('#productRegMenu .nav-link:first').addClass('active');

    // --- 카테고리 2차 동적 로드 (AJAX 기반) ---
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
        if (isNaN(number) || number === null || number === '') return '';
        return new Intl.NumberFormat('ko-KR').format(number);
    }

    function parseCurrency(str) {
        if (typeof str !== 'string' || str.trim() === '') return 0;
        return parseInt(str.replace(/[^0-9]/g, '')) || 0;
    }

    $('#basePrice').on('input', function() {
        var value = parseCurrency($(this).val());
        $(this).val(formatCurrency(value));
        calculateFinalPrice();
    });

    $('#discountRate').on('input', function() {
        calculateFinalPrice();
    });

    function calculateFinalPrice() {
        var basePrice = parseCurrency($('#basePrice').val());
        var discountRate = parseFloat($('#discountRate').val()) || 0;
        var finalPrice = basePrice * (1 - (discountRate / 100));
        $('#finalPrice').val(formatCurrency(Math.round(finalPrice)));
    }
    calculateFinalPrice();

    // 성별 옵션 버튼 클릭 이벤트
    $(document).on('click', '.option-gender-btn', function() {
        $('.option-gender-btn').removeClass('active');
        $(this).addClass('active');
        $('#genderOption').val($(this).data('value')).trigger('input');
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
        var selectedSizes = [];

        $('#colorOptionValues .option-value-btn.active').each(function() {
            selectedColors.push($(this).data('value'));
        });

        $('#sizeOptionValues .option-value-btn.active').each(function() {
            selectedSizes.push($(this).data('value'));
        });

        // 기존에 생성된 숨겨진 옵션 필드 제거 (중복 방지)
        $('#productRegistrationForm input[name^="productOptionDetails"]').remove();

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
                if (tempGender) combinationName += ' ' + tempGender;
                if (color !== '') combinationName += ' ' + color;
                if (size !== '') combinationName += ' ' + size;
                combinationsForTable.push(combinationName.trim());
            });
        });

        // 중복 조합 제거 및 정렬
        combinationsForTable = [...new Set(combinationsForTable)];
        combinationsForTable.sort();

        // 옵션 조합별 재고 입력 필드 생성
        combinationsForTable.forEach(function(combination, index) {
            const colorIndex = index % tempColors.length;
            const sizeIndex = Math.floor(index / tempColors.length);
            const colorValue = tempColors[colorIndex] || '';
            const sizeValue = tempSizes[sizeIndex] || '';

            var rowHtml = `
                <tr>
                    <td>${combination}
                        <input type="hidden" name="productOptionCombinations[${index}].combNm" value="${combination}">
                        <input type="hidden" name="productOptionCombinations[${index}].optVlNo1" value="${selectedGender || ''}">
                        <input type="hidden" name="productOptionCombinations[${index}].optVlNo2" value="${colorValue}">
                        <input type="hidden" name="productOptionCombinations[${index}].optVlNo3" value="${sizeValue}">
                    </td>
                    <td>
                        <input type="number" 
                               class="form-control form-control-sm" 
                               name="productOptionCombinations[${index}].quantity" 
                               placeholder="수량" 
                               min="0" 
                               required
                               value="0">
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
            $fileInputLabel.text(isThumbnail ? '상품 썸네일 업로드 (1장)' : 
                             (maxImages === 15 ? '대표 이미지 업로드 (최소 1장, 최대 15장)' : 
                              '상세 페이지 이미지 업로드 (최소 1장, 최대 20장)'));
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

        if (files && files.length > 0) {
            inputElement.files = files;
            $(inputElement).trigger('change');
        }
    });

    // 이미지 미리보기 삭제 버튼
    $(document).on('click', '.image-upload-item .remove-btn', function() {
        var $itemToRemove = $(this).closest('.image-upload-item');
        var inputId = $(this).data('target-input');
        var fileIndex = $(this).data('file-index');
        var inputElement = document.getElementById(inputId);
        
        const currentFiles = Array.from(inputElement.files);
        const newFiles = currentFiles.filter((file, i) => i !== fileIndex);

        const dataTransfer = new DataTransfer();
        newFiles.forEach(file => dataTransfer.items.add(file));

        inputElement.files = dataTransfer.files;
        $itemToRemove.remove();

        var $previewContainer = $(inputElement).closest('.form-group').find('.image-upload-preview');
        if ($previewContainer.children('.image-upload-item').length === 0) {
            $previewContainer.removeClass('has-images').append('<p class="text-center">클릭 또는 파일을 여기에 끌어다 놓으세요.</p>');
        }

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

        $previewContainer.find('.image-upload-item').each(function(idx) {
            $(this).find('.remove-btn').data('file-index', idx);
        });
    });

    // 하단 버튼 액션
    $('#previewBtn').on('click', function() {
        alert('상품 미리보기 기능 (구현 필요)');
    });
    
    $('#tempSaveBtn').on('click', function() {
        alert('상품 임시 저장 기능 (구현 필요)');
    });

    // 취소 버튼 클릭 이벤트
    $('#cancelBtn').on('click', function() {
        if (confirm('작성 중인 내용을 취소하고 목록으로 돌아가시겠습니까?')) {
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
            $input.trigger('input'); // Initial count
        }
    }

    setupCharCounter('productName', 'productNameCounter');
    setupCharCounter('productDescription', 'productDescriptionCounter');
    setupCharCounter('videoUrl', 'videoUrlCounter');
    setupCharCounter('productTags', 'productTagsCounter');
    setupCharCounter('optionProductName', 'optionProductNameCounter');

    // Initial calls to populate options and combinations
    populateColorOptions();
    $('#sizeOptionType').trigger('change'); 
    updateOptionCombinations();

    // 입력 시 에러 표시 제거
    $('input, select, textarea, .option-gender-btn, .option-value-btn').on('input change click', function() {
        if ($('#productRegistrationForm').hasClass('submitted')) {
            const $this = $(this);
            
            if ($this.is('input') || $this.is('select') || $this.is('textarea')) {
                $this.removeClass('is-invalid');
            }
            
            if ($this.is('input[type="file"]')) {
                $this.next('.custom-file-label').removeClass('error-border');
            }
            
            const $parentOptionGroup = $this.closest('.option-group-item.mb-4'); 
            if ($parentOptionGroup.length) {
                let groupHasError = false;

                if ($parentOptionGroup.find('#genderOptionGroup').length) {
                    if ($('#genderOption').val() === '') {
                        groupHasError = true;
                    }
                }
                
                $parentOptionGroup.find('input[required]:not([type="hidden"]), textarea[required]').each(function() {
                    if (!$(this).val() || $(this).val().trim() === '') {
                        $(this).removeClass('is-invalid'); 
                        groupHasError = true;
                        return false; 
                    }
                });

                $parentOptionGroup.find('input[name="optionCombinationStocks"]').each(function() {
                    if (!$(this).val() || parseInt($(this).val()) < 0) {
                        $(this).removeClass('is-invalid');
                        groupHasError = true; 
                        return false; 
                    }
                });

                if (!groupHasError) {
                    $parentOptionGroup.removeClass('error-border');
                }
            } else {
                const $parent = $this.closest('.form-control-with-labels, .form-group, .category-group');
                if ($parent.length && !$parent.closest('.option-group-item.mb-4').length) { 
                    $parent.removeClass('error-border');
                }
            }
        }
    });

    // --- 폼 제출 시 숫자 필드에서 쉼표 제거 및 유효성 검사 ---
    $('#productRegistrationForm').on('submit', function(event) {
        event.preventDefault(); 
        $(this).addClass('submitted');
        
        $('.is-invalid').removeClass('is-invalid');
        $('.error-border').removeClass('error-border');
        
        // 숫자 필드의 쉼표 제거
        const basePriceInput = document.getElementById('basePrice');
        if (basePriceInput) basePriceInput.value = basePriceInput.value.replace(/,/g, '');
        
        const maxPurchaseInput = document.getElementById('maxPurchase');
        if (maxPurchaseInput) maxPurchaseInput.value = maxPurchaseInput.value.replace(/,/g, '');
        
        $('input[name="optionCombinationStocks"]').each(function() {
            $(this).val($(this).val().replace(/,/g, ''));
        });

        // 유효성 검사
        let isValid = true;
        let errorMessage = "다음 필수 항목들을 입력하거나 선택해주세요:<br>";
        let errorFields = [];

        // 1. 모든 섹션의 필수 입력 필드 검사
        $('input[required]:not(#genderOption), select[required]:not(#sizeOptionType), textarea[required]').each(function() {
            const $input = $(this);
            const value = $input.val();
            
            if ($input.is('input[type="file"]')) {
                const $fileInputLabel = $input.closest('.custom-file').find('.custom-file-label'); 
                if ($input[0].files.length === 0) {
                    let fieldName = $input.next('label').find('.file-name').text().replace(/\s*(\(\s*(최소)?\d+장,\s*(최대)?\d+장\))?\s*(\(\d+장\))?\s*\*\s*$/, '').trim();
                    errorMessage += `- ${fieldName}<br>`;
                    isValid = false;
                    if ($fileInputLabel.length && !errorFields.includes($fileInputLabel[0])) { 
                        errorFields.push($fileInputLabel[0]); 
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
                if (!errorFields.includes($input[0])) { 
                    errorFields.push($input[0]);
                }
            }
        });

        // 2. 카테고리 2차 선택 검사
        if ($('#productCategory2Group').is(':visible') && $('#productCategory2').val() === '') {
            errorMessage += `- 2차 카테고리<br>`;
            isValid = false;
            if (!errorFields.includes($('#productCategory2')[0])) { 
                errorFields.push($('#productCategory2')[0]);
            }
        }

        // 3. 옵션 설정 섹션 검사
        const $genderOption = $('#genderOption');
        const $selectedColors = $('#colorOptionValues .option-value-btn.active');
        const $selectedSizes = $('#sizeOptionValues .option-value-btn.active');
        const $optionProductName = $('#optionProductName');
        const $sizeOptionType = $('#sizeOptionType');

        // 옵션 조합 제품명
        if ($optionProductName.val().trim() === '') {
            errorMessage += `- 옵션 조합 제품명<br>`;
            isValid = false;
            if (!errorFields.includes($optionProductName[0])) { 
                errorFields.push($optionProductName[0]); 
            }
        }
        
        // 성별 옵션 그룹 유효성 검사
        if ($genderOption.val() === '') {
            errorMessage += `- 성별 옵션<br>`;
            isValid = false;
            const $genderGroupParent = $('#genderOptionGroup').closest('.option-group-item.mb-4');
            if ($genderGroupParent.length && !errorFields.includes($genderGroupParent[0])) {
                errorFields.push($genderGroupParent[0]); 
            }
        }
        
        // 색상 옵션 그룹 유효성 검사
        if ($selectedColors.length === 0) {
            errorMessage += `- 색상 옵션<br>`;
            isValid = false;
            const $colorGroupParent = $('#colorOptionValues').closest('.option-group-item.mb-4');
            if ($colorGroupParent.length && !errorFields.includes($colorGroupParent[0])) {
                errorFields.push($colorGroupParent[0]); 
            }
        }

        // 사이즈 유형 및 사이즈 옵션 그룹 유효성 검사
        let sizeOptionGroupError = false;
        if ($sizeOptionType.val() === 'none') {
            errorMessage += `- 사이즈 유형<br>`;
            isValid = false;
            sizeOptionGroupError = true;
            if (!errorFields.includes($sizeOptionType[0])) {
                errorFields.push($sizeOptionType[0]);
            }
        }
        
        if ($sizeOptionType.val() !== 'none' && $selectedSizes.length === 0) {
            errorMessage += `- 사이즈 옵션<br>`;
            isValid = false;
            sizeOptionGroupError = true;
        }

        if (sizeOptionGroupError) {
            const $sizeOptionGroupParent = $('#sizeOptionValues').closest('.option-group-item.mb-4');
            if ($sizeOptionGroupParent.length && !errorFields.includes($sizeOptionGroupParent[0])) { 
                errorFields.push($sizeOptionGroupParent[0]);
            }
        }
        
        // 옵션 조합별 재고 수량 검사
        const $optionCombinationStocks = $('input[name="optionCombinationStocks"]');
        if ($optionCombinationStocks.length > 0) {
            let hasEmptyStock = false;
            $optionCombinationStocks.each(function() {
                if (!$(this).val() || parseInt($(this).val()) < 0) {
                    hasEmptyStock = true;
                    if (!errorFields.includes($(this)[0])) { 
                        errorFields.push($(this)[0]); 
                    }
                }
            });
            if(hasEmptyStock) {
                errorMessage += `- 옵션 조합 재고 수량<br>`;
                isValid = false;
            }
        }

        // 4. 최종 유효성 검사 결과 처리
        if (!isValid) {
            Swal.fire({
                icon: "error",
                title: "입력 오류!",
                html: errorMessage + "<br>모든 필수 항목을 올바르게 채워주세요.",
                confirmButtonText: "확인",
                customClass: {
                    container: 'my-swal-container',
                    popup: 'my-swal-popup',
                    header: 'my-swal-header',
                    title: 'my-swal-title',
                    content: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button'
                }
            }).then(() => {
                errorFields.forEach(fieldElement => {
                    const $field = $(fieldElement);

                    if ($field.is('input') || $field.is('select') || $field.is('textarea')) {
                        $field.addClass('is-invalid');
                        const $parentFormGroup = $field.closest('.form-control-with-labels, .form-group, .category-group');
                        if ($parentFormGroup.length && !$parentFormGroup.closest('.option-group-item.mb-4').length) {
                            $parentFormGroup.addClass('error-border');
                        }
                    }
                    else if ($field.is('.custom-file-label')) {
                        $field.addClass('error-border');
                    }
                    else if ($field.is('.option-group-item.mb-4')) {
                        $field.addClass('error-border');
                    }
                });

                if (errorFields.length > 0) {
                    const $firstErrorField = $(errorFields[0]);
                    const $targetSection = $firstErrorField.closest('.content-section');
                    if ($targetSection.length > 0) {
                        const targetSectionId = $targetSection.attr('id');
                        $('#productRegMenu .nav-link').removeClass('active');
                        $(`[data-section="${targetSectionId}"]`).addClass('active');
                        $('.content-section').removeClass('active');
                        $('#' + targetSectionId).addClass('active');
                        
                        let $scrollToElement = $firstErrorField;
                        if ($firstErrorField.is('input[type="hidden"]')) {
                            if ($firstErrorField.attr('id') === 'genderOption') {
                                $scrollToElement = $('#genderOptionGroup');
                            } else {
                                const $relatedVisible = $firstErrorField.closest('.form-group').find('input:not([type="hidden"]), select, textarea, .image-upload-preview, .option-gender-btn, .option-value-buttons').first();
                                if ($relatedVisible.length > 0) {
                                    $scrollToElement = $relatedVisible;
                                }
                            }
                        }

                        if ($scrollToElement.length > 0) {
                            $('html, body').animate({
                                scrollTop: $scrollToElement.offset().top - ($(window).height() / 4)
                            }, 500);
                        }
                    }
                }
            });
            return false;
        }

        console.log("폼 제출: 모든 유효성 검사 통과");
        this.submit();
    });
});
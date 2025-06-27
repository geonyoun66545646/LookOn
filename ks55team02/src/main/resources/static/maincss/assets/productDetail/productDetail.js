$(document).ready(function() {
    console.log("DOM이 로드되었습니다. 스크립트 실행 시작.");

    const currentProductId = 'product_123';
    
    // .product-price 클래스를 가진 요소의 data-price 속성에서 기본 가격을 가져옵니다.
    const baseProductPrice = parseInt($('.product-price').data('price')) || 59000; 
    console.log("동적으로 가져온 기본 상품 가격:", baseProductPrice);


    // DOM 요소 변수
    const colorSelect = $('.details-row-color .product-option-select');
    const sizeSelect = $('.details-row-size .product-option-select');

    const totalPriceSpan = $('#totalPrice');
    const selectionSummary = $('#selectionSummary');
    const selectedOptionsContainer = $('#selectedOptionsContainer');
    const selectedOptionCurrentPriceSpan = $('#selectedOptionCurrentPrice'); 

    // 선택된 옵션 저장 배열
    let selectedOptions = [];

    // 가격 포맷팅 함수
    function formatPrice(price) {
        return price.toLocaleString('ko-KR') + '원';
    }

    // 총 가격 계산 함수
    function updateTotalPrice() {
        console.log("updateTotalPrice 함수 호출됨.");
        const total = selectedOptions.reduce((sum, option) => {
            // 옵션별 추가 가격 없이, 기본 상품 가격에 수량만 곱합니다.
            return sum + (baseProductPrice * option.quantity); 
        }, 0);
        
        totalPriceSpan.text(`${formatPrice(total)}`);
        console.log("총 상품금액 업데이트:", formatPrice(total));
    }

    // 현재 선택된 단일 옵션 조합의 가격 미리보기 업데이트 함수
    function updateCurrentSelectionPricePreview() {
        console.log("updateCurrentSelectionPricePreview 함수 호출됨.");
        const selectedColor = colorSelect.val();
        const selectedSize = sizeSelect.val();
        console.log("현재 선택된 색상 값:", selectedColor, "현재 선택된 사이즈 값:", selectedSize);

        if (selectedColor && selectedSize) {
            selectedOptionCurrentPriceSpan.text(formatPrice(baseProductPrice));
            console.log("미리보기 가격 업데이트:", formatPrice(baseProductPrice));
        } else {
            selectedOptionCurrentPriceSpan.text('옵션을 모두 선택해주세요.');
            console.log("미리보기 메시지 업데이트: 옵션을 모두 선택해주세요.");
        }
    }

    // 옵션 추가 또는 업데이트 함수
    function addOrUpdateOption() {
        console.log("addOrUpdateOption 함수 호출됨.");
        const selectedColor = colorSelect.val();
        const selectedSize = sizeSelect.val();
        const colorText = colorSelect.find('option:selected').text();
        const sizeText = sizeSelect.find('option:selected').text();
        console.log("addOrUpdateOption 내부 - 선택된 색상:", selectedColor, "선택된 사이즈:", selectedSize);

        if (!selectedColor || !selectedSize) {
            console.log("색상 또는 사이즈가 모두 선택되지 않아 옵션 추가를 건너뜜.");
            return;
        }

        const existingOptionIndex = selectedOptions.findIndex(option => 
            option.color === colorText && option.size === sizeText
        );

        if (existingOptionIndex === -1) {
            selectedOptions.push({
                color: colorText,
                size: sizeText,
                quantity: 1
            });
            console.log("새로운 옵션 추가됨:", {color: colorText, size: sizeText, quantity: 1});
            updateOptionsDisplay(); // 이 함수 내부에서 updateTotalPrice() 호출
            
            colorSelect.val('');
            sizeSelect.val('');
            updateCurrentSelectionPricePreview(); 
        } else {
            alert('이미 추가된 옵션입니다.');
            console.log("이미 추가된 옵션입니다.");
            colorSelect.val('');
            sizeSelect.val('');
            updateCurrentSelectionPricePreview();
        }
    }

    // 옵션 삭제 함수
    function removeOption(index) {
        console.log("removeOption 함수 호출됨. 인덱스:", index);
        selectedOptions.splice(index, 1);
        updateOptionsDisplay();
        if (selectedOptions.length === 0) {
            updateCurrentSelectionPricePreview();
        }
        console.log("옵션 삭제 후 현재 selectedOptions:", selectedOptions);
    }

    // 옵션 표시 업데이트 함수
    function updateOptionsDisplay() {
        console.log("updateOptionsDisplay 함수 호출됨.");
        selectedOptionsContainer.empty();

        if (selectedOptions.length > 0) {
            selectionSummary.show();
            console.log("선택된 옵션 요약 보이기.");
            
            selectedOptions.forEach((option, index) => {
                // ⭐ 여기 수정됨: 각 옵션 아이템 옆에 가격을 추가합니다. ⭐
                const optionElement = $(`
                    <div class="selected-option-item-custom">
                        <div class="option-name-display-custom">
                            <span>${option.color} ${option.size}</span>
                            <span class="option-item-price">${formatPrice(baseProductPrice)}</span> </div>
                        <div class="option-controls-custom">
                            <button class="qty-control-button minus" type="button" data-index="${index}">-</button>
                            <input type="number" class="option-qty-input-custom" 
                                value="${option.quantity}" min="1" max="10" data-index="${index}">
                            <button class="qty-control-button plus" type="button" data-index="${index}">+</button>
                            <button class="remove-option-button" type="button" 
                                    data-index="${index}">×</button>
                        </div>
                    </div>
                `);
                selectedOptionsContainer.append(optionElement);
            });
        } else {
            selectionSummary.hide();
            console.log("선택된 옵션이 없어 요약 숨기기.");
        }

        updateTotalPrice();
    }

    // --- 이벤트 리스너 ---

    colorSelect.on('change', function() {
        console.log("색상 드롭다운 'change' 이벤트 발생.");
        updateCurrentSelectionPricePreview();
        addOrUpdateOption();
    });
    sizeSelect.on('change', function() {
        console.log("사이즈 드롭다운 'change' 이벤트 발생.");
        updateCurrentSelectionPricePreview();
        addOrUpdateOption();
    });

    // 수량 입력 필드 변경
    $(document).on('change', '.option-qty-input-custom', function() {
        console.log("수량 입력 필드 'change' 이벤트 발생.");
        const index = $(this).data('index');
        let newQty = parseInt($(this).val()) || 1;
        newQty = Math.max(1, Math.min(10, newQty));
        selectedOptions[index].quantity = newQty;
        $(this).val(newQty);
        updateTotalPrice();
    });

    // 수량 증가 버튼
    $(document).on('click', '.qty-control-button.plus', function() {
        console.log("수량 증가 버튼 클릭됨.");
        const index = $(this).data('index');
        const qtyInput = $(this).siblings('.option-qty-input-custom');
        let currentQty = parseInt(qtyInput.val());
        if (currentQty < 10) {
            currentQty++;
            selectedOptions[index].quantity = currentQty;
            qtyInput.val(currentQty);
            updateTotalPrice();
        }
    });

    // 수량 감소 버튼
    $(document).on('click', '.qty-control-button.minus', function() {
        console.log("수량 감소 버튼 클릭됨.");
        const index = $(this).data('index');
        const qtyInput = $(this).siblings('.option-qty-input-custom');
        let currentQty = parseInt(qtyInput.val());
        if (currentQty > 1) {
            currentQty--;
            selectedOptions[index].quantity = currentQty;
            qtyInput.val(currentQty);
            updateTotalPrice();
        }
    });

    // 옵션 삭제 버튼
    $(document).on('click', '.remove-option-button', function() {
        console.log("옵션 삭제 버튼 클릭됨.");
        const index = $(this).data('index');
        removeOption(index);
    });

    // 기존 좋아요/장바구니 로직은 변경 없음
    $('.btn-wishlist').on('click', function(e) {
        e.preventDefault();
        $(this).toggleClass('liked');
        let currentLikes = parseInt($('.likes-count').text());
        $('.likes-count').text($(this).hasClass('liked') ? currentLikes + 1 : currentLikes - 1);
        console.log("위시리스트 버튼 클릭됨.");
    });

    $('.btn-cart').on('click', function(e) {
        e.preventDefault();
        console.log("장바구니 버튼 클릭됨.");

        if (selectedOptions.length === 0) {
            alert('최소 하나 이상의 옵션을 추가해주세요.');
            console.log("장바구니 추가 실패: 옵션 없음.");
            return;
        }

        let allItemsAdded = true;
        selectedOptions.forEach(option => {
            const cartKey = `cart_${currentProductId}_${option.color.replace(/\s+/g, '_')}_${option.size.replace(/\s+/g, '_')}`;
            if (localStorage.getItem(cartKey)) {
                alert(`[${option.color} ${option.size}] 옵션은 이미 장바구니에 있습니다.`);
                allItemsAdded = false; 
                console.log(`장바구니 추가 실패: [${option.color} ${option.size}] 이미 존재.`);
            } else {
                localStorage.setItem(cartKey, JSON.stringify({
                    productId: currentProductId, 
                    color: option.color,
                    size: option.size,
                    quantity: option.quantity,
                    price: baseProductPrice // 개별 옵션 아이템의 가격은 기본 상품 가격
                }));
                console.log(`장바구니에 추가됨: ${option.color} ${option.size}`);
            }
        });
        
        if (allItemsAdded) {
             alert('장바구니에 추가되었습니다.');
             console.log("모든 항목 장바구니에 추가 완료.");
        }
    });

    // 페이지 로드 시 초기화
    updateCurrentSelectionPricePreview(); // 페이지 로드 시 "옵션을 모두 선택해주세요." 표시
    updateOptionsDisplay(); // 총 가격 및 선택된 옵션 컨테이너 초기화 (필요하다면)
    console.log("페이지 로드 시 초기화 완료.");
});
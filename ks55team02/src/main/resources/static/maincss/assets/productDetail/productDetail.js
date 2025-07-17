$(document).ready(function() {
    console.log("productDetail.js 파일 실행됨.");

    // --- 1. 전역 변수 및 요소 선택 ---
    const colorSelect = $('.details-row-color .product-option-select');
    const sizeSelect = $('.details-row-size .product-option-select');
    const totalPriceSpan = $('#totalPrice');
    const selectionSummary = $('#selectionSummary');
    const selectedOptionsContainer = $('#selectedOptionsContainer');
    const selectedOptionCurrentPriceSpan = $('#selectedOptionCurrentPrice');
    let selectedOptions = [];
    

    // 이미지 갤러리 관련 요소
    const mainImage = document.getElementById('product-detail-zoom');
    const galleryItems = document.querySelectorAll('.product-detail-gallery-item');
    const imageCounter = document.querySelector('.product-detail-image-counter span');
    const thumbnailGallery = document.querySelector('.product-detail-image-gallery');
    const thumbUpButton = document.querySelector('.product-detail-thumbnail-nav-btn.up');
    const thumbDownButton = document.querySelector('.product-detail-thumbnail-nav-btn.down');

    // --- 2. 초기화 및 헬퍼 함수 ---

    function formatPrice(price) {
        return price.toLocaleString('ko-KR') + '원';
    }

    function getBaseProductPrice() {
        const priceText = $('.final-price-wrapper .final-price').text() || '0';
        return parseInt(priceText.replace(/[^0-9]/g, ''), 10) || 0;
    }

    function updateTotalPrice() {
        const basePrice = getBaseProductPrice();
        let total = selectedOptions.reduce((sum, option) => sum + (basePrice * option.quantity), 0);
        totalPriceSpan.text(formatPrice(total));
    }

    function updateOptionsDisplay() {
        selectedOptionsContainer.empty();
        const basePrice = getBaseProductPrice();

        if (selectedOptions.length > 0) {
            selectionSummary.show();

            selectedOptions.forEach((option, index) => {
                const optionElement = $(`
                    <div class="selected-option-item-custom" data-index="${index}">
                        <div class="option-name-display-custom">
                            <span>${option.color} / ${option.size}</span>
                            <span class="option-item-price">${formatPrice(basePrice)}</span>
                        </div>
                        <div class="option-controls-custom">
                            <button class="qty-control-button minus" type="button">-</button>
                            <input type="text" class="option-qty-input-custom" value="${option.quantity}" readonly>
                            <button class="qty-control-button plus" type="button">+</button>
                            <button class="remove-option-button" type="button">×</button>
                        </div>
                    </div>
                `);
                selectedOptionsContainer.append(optionElement);
            });
        } else {
            selectionSummary.hide();
        }
        updateTotalPrice();
    }
    
	/**
	 * ⭐⭐[최종] 선택된 옵션 값들의 조합에 해당하는 상품 재고 상태 데이터를 찾습니다.
	 * @param {string[]} selectedOptVlNos - 사용자가 선택한 옵션 값 ID들의 배열
	 * @returns {object | null} - 찾은 재고 상태 객체 또는 null
	 */
	function findProductStatus(selectedOptVlNos) {
	    // 선택된 옵션 ID들을 항상 동일한 순서로 정렬하여 비교용 문자열을 만듭니다.
	    const sortedSelectedOptVlNos = selectedOptVlNos.sort().join(',');

	    // productStatusData 배열을 순회하며 일치하는 조합을 찾습니다.
	    for (const status of productStatusData) {
	        const sortedStatusOptVlNos = status.opt_vl_nos.split(',').sort().join(',');
	        if (sortedSelectedOptVlNos === sortedStatusOptVlNos) {
	            return status; // 객체 전체 반환
	        }
	    }
	    return null;
	}
	
	/* 옵션 */
	/**
	 * ⭐⭐[최종 수정] 옵션 선택 시 처리 로직입니다.
	 * 재고를 확인하고 UI를 업데이트합니다.
	 */
	function handleOptionSelection() {
	    // 1. 색상과 사이즈 옵션 요소를 선택합니다.
	    const colorSelect = $('.details-row-color .product-option-select');
	    const sizeSelect = $('.details-row-size .product-option-select');
	    
	    // 2. 색상과 사이즈의 텍스트와 값을 가져옵니다.
	    const selectedColorText = colorSelect.find('option:selected').text();
	    const selectedSizeText = sizeSelect.find('option:selected').text();
	    
	    const selectedColorOptNo = colorSelect.val();
	    const selectedSizeOptNo = sizeSelect.val();
	    
	    // 3. 성별 옵션 값은 productDataOptions에서 고정된 값을 찾아서 사용합니다.
	    const genderOptionData = productDataOptions.find(opt => opt.optNm === '성별');
	    const selectedGenderText = genderOptionData ? genderOptionData.optionValues[0].vlNm : '';
	    const selectedGenderOptNo = genderOptionData ? genderOptionData.optionValues[0].optVlNo : '';

	    // 4. 모든 필수 옵션을 선택했는지 확인합니다.
	    if (!selectedGenderOptNo || !selectedColorOptNo || !selectedSizeOptNo) {
	        selectedOptionCurrentPriceSpan.text('옵션을 모두 선택해주세요.');
	        selectionSummary.hide();
	        return;
	    }
	    
	    // 5. 재고 데이터에서 일치하는 3가지 조합을 찾습니다.
	    const foundStatus = findProductStatus([selectedGenderOptNo, selectedColorOptNo, selectedSizeOptNo]);

	    // 6. 재고 조합이 존재하지 않거나, 재고 수량이 0인 경우
	    if (!foundStatus || foundStatus.sel_psblty_qntty <= 0) {
	    	alert('선택하신 옵션 조합은 재고가 없거나 품절입니다.');
	        selectedOptionCurrentPriceSpan.text('재고 없음');
	        selectionSummary.hide();
	        return;
	    }

	    // 7. 이미 추가된 옵션인지 확인
	    const isExisting = selectedOptions.some(opt => opt.gdsSttsNo === foundStatus.gds_stts_no);

	    if (isExisting) {
	        alert('이미 추가된 옵션입니다.');
	    } else {
	        selectedOptions.push({
	            gender: selectedGenderText,
	            color: selectedColorText,
	            size: selectedSizeText,
	            quantity: 1,
	            gdsSttsNo: foundStatus.gds_stts_no
	        });
	        updateOptionsDisplay();
	    }
	    
	    // 8. 옵션 선택 후 가격을 다시 표시
	    selectedOptionCurrentPriceSpan.text(formatPrice(getBaseProductPrice()));
	}

    // --- 3. 이벤트 리스너 등록 ---

    if (mainImage && galleryItems.length > 0) {
        const totalImages = galleryItems.length;
        const updateImageCounter = (currentIndex) => {
            if (imageCounter) {
                const counterContainer = imageCounter.closest('.product-detail-image-counter');
                if (counterContainer) {
                    counterContainer.innerHTML = `${currentIndex + 1}/${totalImages} <i class="icon-arrows"></i>`;
                }
            }
        };
        galleryItems.forEach((item, index) => {
            item.addEventListener('click', function(e) {
                e.preventDefault();
                galleryItems.forEach(thumb => thumb.classList.remove('active'));
                this.classList.add('active');
                const newImageSrc = this.getAttribute('data-image');
                mainImage.src = newImageSrc;
                updateImageCounter(index);
            });
        });
        updateImageCounter(0);
    }

    if (thumbnailGallery && thumbUpButton && thumbDownButton) {
        const scrollAmount = 100;
        thumbUpButton.addEventListener('click', () => {
            thumbnailGallery.scrollBy({ top: -scrollAmount, behavior: 'smooth' });
        });
        thumbDownButton.addEventListener('click', () => {
            thumbnailGallery.scrollBy({ top: scrollAmount, behavior: 'smooth' });
        });
    }

    $('.product-option-select').on('change', handleOptionSelection);

    selectedOptionsContainer.on('click', '.qty-control-button, .remove-option-button', function() {
        const $target = $(this);
        const $item = $target.closest('.selected-option-item-custom');
        if ($item.length === 0) return;
        const index = $item.data('index');
        let option = selectedOptions[index];

        if ($target.hasClass('plus')) {
            if (option.quantity < 10) { // 임의의 최대 수량
                option.quantity++;
            } else {
                alert('최대 구매 수량입니다.');
            }
        } else if ($target.hasClass('minus') && option.quantity > 1) {
            option.quantity--;
        } else if ($target.hasClass('remove-option-button')) {
            selectedOptions.splice(index, 1);
        }
        updateOptionsDisplay();
    });

    /**
     * ⭐ 최종 수정된 장바구니 버튼 클릭 이벤트
     * 이제 optNo 대신 gdsSttsNo를 전송합니다.
     */
    $('.btn-cart').on('click', function(e) {
        e.preventDefault();

        if (selectedOptions.length === 0) {
            alert('최소 하나 이상의 옵션을 추가해주세요.');
            return;
        }

        const gdsNo = currentProductGdsNo;
        const storeId = currentProductStoreId;

		const payload = {
		    gdsNo: gdsNo,
		    storeId: storeId,
		    selectedOptions: selectedOptions.map(option => ({
		        // ⭐⭐ 키 이름을 gdsSttsNo로 변경
		        gdsSttsNo: option.gdsSttsNo, 
		        quantity: option.quantity
		    }))
		};

        console.log("전송할 페이로드:", JSON.stringify(payload, null, 2));

        $.ajax({
            url: '/api/cart/add',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function(response) {
                if (confirm('장바구니에 상품이 추가되었습니다. 장바구니로 이동하시겠습니까?')) {
                    window.location.href = '/cart';
                }
            },
            error: function(xhr, status, error) {
                console.error("장바구니 추가 중 오류 발생:", error);
                let errorMessage = '장바구니 추가 중 오류가 발생했습니다.';
                if (xhr.status === 401) {
                    errorMessage = '로그인이 필요합니다.';
                } else if (xhr.responseText) {
                    try {
                        const errorResponse = JSON.parse(xhr.responseText);
                        errorMessage = errorResponse.message || errorMessage;
                    } catch (e) {
                        errorMessage = xhr.responseText;
                    }
                }
                alert(errorMessage);
            },
            complete: function() {
                selectedOptions = [];
                updateOptionsDisplay();
            }
        });
    });

    /* ========================== 구매하기 버튼 2025.07.11 gy ========================== */
    $('.btn-buy-now').on('click', function(e) {
        e.preventDefault();
        const productName = $('.product-details .product-title').text().trim();
        const productImageSrc = $('.product-main-image img').attr('src');
        const productGdsNo = typeof currentProductGdsNo !== 'undefined' ? currentProductGdsNo : null;
        if (selectedOptions.length === 0) {
            alert('구매하시려면 최소 하나 이상의 옵션을 선택해주세요.');
            return;
        }
        if (confirm('선택하신 상품을 바로 구매하시겠습니까?')) {
            let buyNowData = [];
            selectedOptions.forEach(option => {
                const uniqueItemId = `${productName}_${option.color}_${option.size}`;
                const basePrice = getBaseProductPrice();
                buyNowData.push({
                    id: uniqueItemId,
                    gdsNo: productGdsNo,
                    name: `${productName} ${option.color} ${option.size}`,
                    price: basePrice,
                    quantity: option.quantity,
                    image: productImageSrc,
                    color: option.color,
                    size: option.size
                });
            });
            sessionStorage.setItem('buy_now_data', JSON.stringify(buyNowData));
            window.location.href = '/checkout';
        } else {
            console.log("구매하기 취소됨.");
        }
    });

    /* =================================================================== */
    /* ⭐ [새로 추가] 비슷한 상품 슬라이더 초기화 ⭐ */
    /* =================================================================== */
    const similarSwiperContainer = document.querySelector('.weekly-best-swiper');
    if (similarSwiperContainer) {
        const similarProductsSwiper = new Swiper(similarSwiperContainer, {
            loop: false,
            autoplay: false,
            slidesPerView: 'auto',
            spaceBetween: 1,
            navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev',
            },
        });
    }
    
    const gallery = document.querySelector('.product-detail-image-gallery');
    if (gallery) {
        gallery.addEventListener('wheel', function(event) {
            if (event.deltaY !== 0) {
                event.preventDefault();
                window.scrollBy(0, event.deltaY);
            }
        });
        let isDown = false;
        let startY;
        let scrollTop;
        gallery.addEventListener('mousedown', (e) => {
            isDown = true;
            gallery.classList.add('active');
            startY = e.pageY - gallery.offsetTop;
            scrollTop = gallery.scrollTop;
        });
        gallery.addEventListener('mouseleave', () => {
            isDown = false;
            gallery.classList.remove('active');
        });
        gallery.addEventListener('mouseup', () => {
            isDown = false;
            gallery.classList.remove('active');
        });
        gallery.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const y = e.pageY - gallery.offsetTop;
            const walk = (y - startY) * 2;
            gallery.scrollTop = scrollTop - walk;
        });
    }
    console.log("페이지 로드 시 모든 스크립트 초기화 완료.");
});
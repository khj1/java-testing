package sample.cafekiosk.spring.api.controller.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import sample.cafekiosk.spring.api.service.product.dto.request.ProductCreateServiceRequest;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

/**
 * 기본적인 유효성 검사가 아닌 도메인 정책에 사용되는 유효성 검사는
 * 따로 분리시킬 필요성이 있다.
 */
public record ProductCreateRequest(
    @NotNull(message = "상품 타입은 필수 입니다.")
    ProductType type,

    @NotNull(message = "상품 판매 상태는 필수입니다.")
    ProductSellingStatus sellingStatus,

    @NotBlank(message = "상품 이름은 필수입니다.")
    String name,

    @Positive(message = "상품 가격은 양수여야 합니다.")
    int price
) {
    public static ProductCreateRequest of(ProductType type, ProductSellingStatus sellingStatus, String name, int price) {
        return new ProductCreateRequest(type, sellingStatus, name, price);
    }

    public ProductCreateServiceRequest toServiceRequest() {
        return new ProductCreateServiceRequest(type, sellingStatus, name, price);
    }
}

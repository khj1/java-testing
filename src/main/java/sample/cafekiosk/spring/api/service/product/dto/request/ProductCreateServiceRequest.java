package sample.cafekiosk.spring.api.service.product.dto.request;

import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

public record ProductCreateServiceRequest(
    ProductType type,
    ProductSellingStatus sellingStatus,
    String name,
    int price
) {
    public static ProductCreateServiceRequest of(ProductType type, ProductSellingStatus sellingStatus, String name, int price) {
        return new ProductCreateServiceRequest(type, sellingStatus, name, price);
    }

    public Product toEntity(String productNumber) {
        return Product.builder()
            .productNumber(productNumber)
            .type(type)
            .sellingStatus(sellingStatus)
            .name(name)
            .price(price)
            .build();
    }
}

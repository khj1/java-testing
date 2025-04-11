package sample.cafekiosk.spring.api.service.product.dto;

import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

public record ProductResponse(
        Long id,
        String productNumber,
        ProductType type,
        ProductSellingStatus sellingType,
        String name,
        int price
) {
    public static ProductResponse of(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductNumber(),
                product.getType(),
                product.getSellingStatus(),
                product.getName(),
                product.getPrice()
        );
    }
}

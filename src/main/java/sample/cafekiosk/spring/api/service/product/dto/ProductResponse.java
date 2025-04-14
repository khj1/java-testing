package sample.cafekiosk.spring.api.service.product.dto;

import sample.cafekiosk.spring.domain.orderproduct.OrderProduct;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

import java.util.List;

public record ProductResponse(
        Long id,
        String productNumber,
        ProductType type,
        ProductSellingStatus sellingStatus,
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

    public static List<ProductResponse> of(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(OrderProduct::getProduct)
                .map(ProductResponse::of)
                .toList();
    }
}

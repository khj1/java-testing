package sample.cafekiosk.spring.api.service.order.response;

import sample.cafekiosk.spring.api.service.product.dto.response.ProductResponse;
import sample.cafekiosk.spring.domain.order.Order;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    int totalPrice,
    LocalDateTime registeredDateTime,
    List<ProductResponse> products
) {
    public static OrderResponse of(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getTotalPrice(),
            order.getRegisteredDateTime(),
            ProductResponse.of(order.getOrderProducts())
        );
    }
}

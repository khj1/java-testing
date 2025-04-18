package sample.cafekiosk.spring.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sample.cafekiosk.spring.api.controller.order.request.OrderSearchCond;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.product.Product;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderQueryRepository {
    Order searchById(Long id);

    List<Product> searchProductsBy(Long id);

    List<Order> searchBy(OrderStatus orderStatus, LocalDateTime dateTime);

    List<OrderResponse> findOrderDtos();

    Order findHighestTotalPriceOrder();

    Integer calculateTotalPriceBy(OrderStatus orderStatus);

    void updateOrderStatusBetween(OrderStatus orderStatus, LocalDateTime start, LocalDateTime end);

    Page<OrderResponse> findOrdersPage(OrderSearchCond condition, Pageable pageable);
}

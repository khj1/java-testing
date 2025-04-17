package sample.cafekiosk.spring.domain.order;

import sample.cafekiosk.spring.domain.product.Product;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderQueryRepository {
    Order searchById(Long id);

    List<Product> searchProductsBy(Long id);

    List<Order> searchBy(OrderStatus orderStatus, LocalDateTime dateTime);
}

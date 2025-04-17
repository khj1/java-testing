package sample.cafekiosk.spring.domain.order;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import sample.cafekiosk.spring.domain.orderproduct.QOrderProduct;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.QProduct;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public OrderQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Order searchById(Long id) {
        QOrder order = QOrder.order;

        return queryFactory
            .select(order)
            .from(order)
            .where(order.id.eq(id))
            .fetchOne();
    }

    @Override
    public List<Product> searchProductsBy(Long id) {
        QOrderProduct orderProduct = QOrderProduct.orderProduct;
        QProduct product = QProduct.product;

        return queryFactory
            .select(orderProduct.product)
            .from(orderProduct)
            .join(orderProduct.product, product)
            .where(orderProduct.order.id.eq(id))
            .fetch();
    }

    @Override
    public List<Order> searchBy(OrderStatus orderStatus, LocalDateTime dateTime) {
        QOrder order = QOrder.order;
        LocalDateTime start = dateTime;
        LocalDateTime end = dateTime == null ? null : dateTime.plusDays(1);

        return queryFactory
            .select(order)
            .from(order)
            .where(
                orderStatusEq(orderStatus),
                dateTimeBetween(start, end)
            )
            .fetch();
    }

    private BooleanExpression orderStatusEq(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return null;
        }
        return QOrder.order.orderStatus.eq(orderStatus);
    }

    private BooleanExpression dateTimeBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        return QOrder.order.registeredDateTime.goe(start)
            .and(QOrder.order.registeredDateTime.lt(end));
    }
}

package sample.cafekiosk.spring.domain.order;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import sample.cafekiosk.spring.api.controller.order.request.OrderSearchCond;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.api.service.product.dto.response.ProductResponse;
import sample.cafekiosk.spring.domain.orderproduct.QOrderProduct;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.QProduct;

import java.time.LocalDateTime;
import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;

@Repository
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public OrderQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
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

    public List<OrderResponse> findOrderDtos() {
        QOrder order = QOrder.order;
        QOrderProduct orderProduct = QOrderProduct.orderProduct;
        QProduct product = QProduct.product;

        return queryFactory
            .from(order)
            .leftJoin(order.orderProducts, orderProduct)
            .leftJoin(orderProduct.product, product)
            .transform(
                groupBy(order.id).list(
                    Projections.constructor(
                        OrderResponse.class,
                        order.id,
                        order.totalPrice,
                        order.registeredDateTime,
                        list(
                            Projections.constructor(
                                ProductResponse.class,
                                product.id,
                                product.productNumber,
                                product.type,
                                product.sellingStatus,
                                product.name,
                                product.price
                            )
                        )
                    )
                )
            );
    }

    @Override
    public Order findHighestTotalPriceOrder() {
        QOrder order = QOrder.order;
        QOrderProduct orderProduct = QOrderProduct.orderProduct;
        QProduct product = QProduct.product;

        Long highestTotalPriceId = queryFactory
            .select(order.id)
            .from(order)
            .leftJoin(order.orderProducts, orderProduct)
            .leftJoin(orderProduct.product, product)
            .groupBy(order.id)
            .orderBy(order.totalPrice.desc())
            .fetchFirst();

        return queryFactory
            .selectFrom(order)
            .leftJoin(order.orderProducts, orderProduct).fetchJoin()
            .leftJoin(orderProduct.product, product).fetchJoin()
            .where(order.id.eq(highestTotalPriceId))
            .fetchOne();
    }

    @Override
    public Integer calculateTotalPriceBy(OrderStatus orderStatus) {
        QOrder order = QOrder.order;
        QOrderProduct orderProduct = QOrderProduct.orderProduct;
        QProduct product = QProduct.product;

        return queryFactory
            .select(product.price.sum())
            .from(order)
            .join(order.orderProducts, orderProduct)
            .join(orderProduct.product, product)
            .groupBy(order.orderStatus)
            .where(order.orderStatus.eq(orderStatus))
            .fetchOne();
    }

    @Override
    public void updateOrderStatusBetween(OrderStatus orderStatus, LocalDateTime start, LocalDateTime end) {
        QOrder order = QOrder.order;

        queryFactory
            .update(order)
            .set(order.orderStatus, orderStatus)
            .where(order.registeredDateTime.goe(start)
                .and(order.registeredDateTime.lt(end)))
            .execute();
    }

    @Override
    public Page<OrderResponse> findOrdersPage(OrderSearchCond condition, Pageable pageable) {
        QOrder order = QOrder.order;
        QOrderProduct orderProduct = QOrderProduct.orderProduct;
        QProduct product = QProduct.product;

        LocalDateTime start = condition.registeredDateTime();
        LocalDateTime end = start == null ? null : start.plusDays(1);

        JPAQuery<Order> query = queryFactory
            .selectFrom(order)
            .leftJoin(order.orderProducts, orderProduct).fetchJoin()
            .leftJoin(orderProduct.product, product).fetchJoin()
            .where(
                orderStatusEq(condition.orderStatus()),
                dateTimeBetween(start, end)
            );

        pageable.getSort().forEach(o -> {
            PathBuilder<Order> path = new PathBuilder<>(Order.class, order.getMetadata());
            query.orderBy(new OrderSpecifier(
                o.isAscending() ? ASC : DESC,
                path.get(o.getProperty())
            ));
        });

        List<Order> orders = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<OrderResponse> dtos = orders.stream()
            .map(OrderResponse::of)
            .toList();


        Long totalCount = queryFactory
            .select(order.count())
            .from(order)
            .where(
                orderStatusEq(condition.orderStatus()),
                dateTimeBetween(start, end)
            )
            .fetchOne();

        return new PageImpl<>(dtos, pageable, totalCount);
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

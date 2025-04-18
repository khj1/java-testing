package sample.cafekiosk.spring.api.controller.order.request;

import sample.cafekiosk.spring.domain.order.OrderStatus;

import java.time.LocalDateTime;

public record OrderSearchCond(
    OrderStatus orderStatus,
    LocalDateTime registeredDateTime
) {
}

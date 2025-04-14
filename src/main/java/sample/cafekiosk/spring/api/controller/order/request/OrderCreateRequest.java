package sample.cafekiosk.spring.api.controller.order.request;

import jakarta.validation.constraints.NotEmpty;
import sample.cafekiosk.spring.api.service.order.request.OrderCreateServiceRequest;

import java.util.List;

public record OrderCreateRequest(
    @NotEmpty(message = "상품 번호가 누락되어선 안됩니다.")
    List<String> productNumbers
) {
    public OrderCreateServiceRequest toServiceRequest() {
        return new OrderCreateServiceRequest(productNumbers);
    }
}

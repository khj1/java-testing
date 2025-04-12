package sample.cafekiosk.spring.api.controller.order.request;

import java.util.List;

public record OrderCreateRequest(
        List<String> productNumbers
) {
}

package sample.cafekiosk.spring.api.service.order.request;

import java.util.List;

public record OrderCreateServiceRequest(
    List<String> productNumbers
) {
}

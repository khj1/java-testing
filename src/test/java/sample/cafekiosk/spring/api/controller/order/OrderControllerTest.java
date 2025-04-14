package sample.cafekiosk.spring.api.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.OrderService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @DisplayName("주문 요청을 토대로 주문을 생성할 수 있다.")
    @Test
    void createOrder() throws Exception {
        List<String> productNumbers = List.of("001", "002");
        OrderCreateRequest request = new OrderCreateRequest(productNumbers);

        mockMvc.perform(post("/api/v1/orders/new")
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.message").value("OK"));
    }

    @DisplayName("주문 요청을 보낼 때 주문 상품이 누락되어선 안된다.")
    @Test
    void createOrderWithoutProductNumbers() throws Exception {
        List<String> emptyProductNumbers = List.of();
        OrderCreateRequest request = new OrderCreateRequest(emptyProductNumbers);

        mockMvc.perform(post("/api/v1/orders/new")
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("상품 번호가 누락되어선 안됩니다."));
    }
}
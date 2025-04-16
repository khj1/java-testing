package sample.cafekiosk.spring.api.controller.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.cafekiosk.spring.ControllerTestSupport;
import sample.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import sample.cafekiosk.spring.api.service.product.dto.response.ProductResponse;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.product.ProductType.HANDMADE;

class ProductControllerTest extends ControllerTestSupport {

    @DisplayName("신규 상품을 등록한다.")
    @Test
    void createProduct() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.of(HANDMADE, SELLING, "아메리카노", 4000);

        mockMvc.perform(
                post("/api/v1/product/new")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @DisplayName("신규 상품을 등록할 때 상품 타입은 필수 값이다.")
    @Test
    void createProductWithoutType() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.of(null, SELLING, "아메리카노", 4000);

        mockMvc.perform(
                post("/api/v1/product/new")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("상품 타입은 필수 입니다."))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @DisplayName("신규 상품을 등록할 때 판매 상태는 필수 값이다.")
    @Test
    void createProductWithoutSellingStatus() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.of(HANDMADE, null, "아메리카노", 4000);

        mockMvc.perform(
                post("/api/v1/product/new")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("상품 판매 상태는 필수입니다."))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @DisplayName("신규 상품을 등록할 때 상품 이름은 필수 값이다.")
    @Test
    void createProductWithoutName() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.of(HANDMADE, SELLING, "", 4000);

        mockMvc.perform(
                post("/api/v1/product/new")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("상품 이름은 필수입니다."))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @DisplayName("신규 상품을 등록할 때 가격은 양수 값이어야 한다.")
    @Test
    void createProductWithNegativePrice() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.of(HANDMADE, SELLING, "아메리카노", -1);

        mockMvc.perform(
                post("/api/v1/product/new")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("상품 가격은 양수여야 합니다."))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @DisplayName("판매 중인 상품을 조회할 수 있다.")
    @Test
    void getSellingProducts() throws Exception {
        List<ProductResponse> result = List.of();
        when(productService.getSellingProducts()).thenReturn(result);

        mockMvc.perform(
                get("/api/v1/product/selling")
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data").isArray());
    }
}
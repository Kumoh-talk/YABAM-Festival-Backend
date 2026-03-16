package com.application.presentation.order.controller;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.CachedOrderList;
import domain.pos.order.repository.SaleOrderCachePort;
import domain.pos.order.service.OrderService;
import fixtures.order.OrderFixture;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private SaleOrderCachePort saleOrderCachePort;

    // ────────────────────────────────────────────────────────────────
    // 영업 주문 슬라이스 조회 (GET /api/v1/sales/{saleId}/orders)
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/sales/{saleId}/orders")
    class GetSaleOrderSlice {

        @Test
        @DisplayName("성공: 캐시 히트 - OrderService 호출 없이 결과 반환")
        void getSaleOrderSlice_CacheHit() throws Exception {
            Long saleId = 1L;
            CachedOrderList.CachedOrder cachedOrder = CachedOrderList.CachedOrder
                    .from(OrderFixture.GENERAL_ORDER());
            given(saleOrderCachePort.getSaleOrderSlice(eq(saleId), anyList(), anyInt(), any()))
                    .willReturn(Optional.of(List.of(cachedOrder)));

            mockMvc.perform(get("/api/v1/sales/{saleId}/orders", saleId)
                            .param("orderStatuses", "ORDERED")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageContents").exists());

            verify(orderService, never()).getSaleOrderSlice(any(), any(), any(), anyInt(), any());
        }

        @Test
        @DisplayName("성공: 캐시 미스 - OrderService 호출 후 DB 결과 반환")
        void getSaleOrderSlice_CacheMiss() throws Exception {
            Long saleId = 1L;
            Order order = OrderFixture.GENERAL_ORDER();
            given(saleOrderCachePort.getSaleOrderSlice(eq(saleId), anyList(), anyInt(), any()))
                    .willReturn(Optional.empty());
            given(orderService.getSaleOrderSlice(any(), eq(saleId), anyList(), anyInt(), any()))
                    .willReturn(new SliceImpl<>(List.of(order)));

            mockMvc.perform(get("/api/v1/sales/{saleId}/orders", saleId)
                            .param("orderStatuses", "ORDERED")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk());

            verify(orderService).getSaleOrderSlice(any(), eq(saleId), anyList(), anyInt(), any());
        }

        @Test
        @DisplayName("성공: bypassCache=true 이면 캐시 조회 없이 DB로 직접 조회")
        void getSaleOrderSlice_BypassCache() throws Exception {
            Long saleId = 1L;
            Order order = OrderFixture.GENERAL_ORDER();
            given(orderService.getSaleOrderSlice(any(), eq(saleId), anyList(), anyInt(), any()))
                    .willReturn(new SliceImpl<>(List.of(order)));

            mockMvc.perform(get("/api/v1/sales/{saleId}/orders", saleId)
                            .param("orderStatuses", "ORDERED")
                            .param("pageSize", "10")
                            .param("bypassCache", "true"))
                    .andExpect(status().isOk());

            // bypassCache=true 이면 캐시 포트 조회가 없어야 한다
            verify(saleOrderCachePort, never()).getSaleOrderSlice(any(), any(), anyInt(), any());
            verify(orderService).getSaleOrderSlice(any(), eq(saleId), anyList(), anyInt(), any());
        }

        @Test
        @DisplayName("성공: 캐시 조회 중 예외 발생 시 DB 폴백")
        void getSaleOrderSlice_CacheExceptionFallsBackToDb() throws Exception {
            Long saleId = 1L;
            Order order = OrderFixture.GENERAL_ORDER();
            given(saleOrderCachePort.getSaleOrderSlice(eq(saleId), anyList(), anyInt(), any()))
                    .willThrow(new RuntimeException("Redis connection error"));
            given(orderService.getSaleOrderSlice(any(), eq(saleId), anyList(), anyInt(), any()))
                    .willReturn(new SliceImpl<>(List.of(order)));

            mockMvc.perform(get("/api/v1/sales/{saleId}/orders", saleId)
                            .param("orderStatuses", "ORDERED")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk());

            verify(orderService).getSaleOrderSlice(any(), eq(saleId), anyList(), anyInt(), any());
        }

        @Test
        @DisplayName("실패: pageSize=0 - @Min 유효성 검증 예외 발생")
        void getSaleOrderSlice_InvalidPageSize() throws Exception {
            // @Validated + @Min 는 ConstraintViolationException 발생
            // @WebMvcTest 환경(전역 ExceptionHandler 없음)에서 예외가 전파됨을 확인
            jakarta.servlet.ServletException thrown = assertThrows(
                    jakarta.servlet.ServletException.class,
                    () -> mockMvc.perform(get("/api/v1/sales/{saleId}/orders", 1L)
                                    .param("orderStatuses", "ORDERED")
                                    .param("pageSize", "0"))
                            .andReturn());
            assertThat(thrown.getCause()).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
        }

        @Test
        @DisplayName("실패: orderStatuses 미입력 - 필수 파라미터 누락으로 400")
        void getSaleOrderSlice_MissingOrderStatuses() throws Exception {
            mockMvc.perform(get("/api/v1/sales/{saleId}/orders", 1L)
                            .param("pageSize", "10"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ────────────────────────────────────────────────────────────────
    // 단건 주문 조회 (GET /api/v1/orders/{orderId})
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/orders/{orderId}")
    class GetOrder {

        @Test
        @DisplayName("성공: 주문 단건 조회")
        void getOrder_Success() throws Exception {
            Long orderId = 1L;
            given(orderService.getOrder(orderId)).willReturn(OrderFixture.GENERAL_ORDER());

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    // ────────────────────────────────────────────────────────────────
    // 영수증별 주문 목록 조회 (GET /api/v1/receipts/{receiptId}/orders)
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/receipts/{receiptId}/orders")
    class GetReceiptOrders {

        @Test
        @DisplayName("성공: 영수증별 주문 목록 조회")
        void getReceiptOrders_Success() throws Exception {
            UUID receiptId = UUID.randomUUID();
            given(orderService.getReceiptOrders(receiptId))
                    .willReturn(List.of(OrderFixture.GENERAL_ORDER()));

            mockMvc.perform(get("/api/v1/receipts/{receiptId}/orders", receiptId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}

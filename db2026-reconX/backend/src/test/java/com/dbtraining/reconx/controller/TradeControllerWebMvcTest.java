package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.security.JwtTokenProvider;
import com.dbtraining.reconx.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ReconxApplication carries @EnableJpaAuditing, which drags JPA auditing beans into
// this web slice and fails with "JPA metamodel must not be empty" (no entities are
// scanned here). Mocking the mapping context satisfies it without booting JPA.
@WebMvcTest(TradeController.class)
@MockBean(JpaMetamodelMappingContext.class)
class TradeControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private TradeService tradeService;
    // TradeController also constructor-injects TradeMapper; @WebMvcTest does not
    // create the MapStruct impl, so it has to be mocked or the context fails to load.
    @MockBean  private TradeMapper tradeMapper;
    // SecurityConfig pulls in JwtAuthenticationFilter, which needs JwtTokenProvider —
    // a @Component outside the web slice, so it must be mocked here too.
    @MockBean  private JwtTokenProvider jwtTokenProvider;

    private TradeRequest validRequest() {
        // Field order matches the current TradeRequest record:
        // (tradeRef, instrumentId, counterpartyId, assetClass, side, quantity, price, tradeDate).
        // tradeRef regex: ^[A-Z]{3}-\d{8}-\d{4}$. Status is NOT a request field — it is set server-side.
        return new TradeRequest(
                "TRD-20260315-9999",
                1L,
                1L,
                "EQUITY",
                "BUY",
                new BigDecimal("100.0000"),
                new BigDecimal("245.50"),
                LocalDate.now());
    }

    @Test
    @WithMockUser(roles = "TRADER")
    @Disabled("Needs TICKET-ADV064 — TradeController.create() is still an "
            + "UnsupportedOperationException stub, so this cannot return 201 yet. "
            + "Re-enable once ADV064 lands and mock the mapper to build the response body.")
    void testCreateTrade_authenticated_returns201() throws Exception {
        // TradeService.create(TradeRequest, String actor) returns the Trade entity;
        // the controller maps it to TradeResponse via TradeMapper.
        when(tradeService.create(any(), any())).thenReturn(new Trade());

        mockMvc.perform(post("/api/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest()))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/trades/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.tradeRef").value("TRD-20260315-9999"));
    }

    @Test
    @Disabled("Needs TICKET-ADV073 + ADV074 — SecurityConfig is still the Day-1 "
            + "anyRequest().permitAll() stub, so there is no 401 path yet (currently 403). "
            + "Re-enable once the stateless JWT + RBAC filter chain lands.")
    void testCreateTrade_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());
    }
}
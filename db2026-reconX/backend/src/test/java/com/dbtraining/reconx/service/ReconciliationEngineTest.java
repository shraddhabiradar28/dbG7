package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {
    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    @DisplayName("exact match on price and qty returns MATCHED")
    void testReconcile_exactMatch_returnsMatched() {
        // given
        EquityTrade internal = equity("EQU-20260603-0001", "100.00", "10", 1L);
        EquityTrade external = equity("EQU-20260603-0001", "100.00", "10", 1L);

        // when
        List<ReconResult> results = engine.reconcile(List.of(internal), List.of(external), ReconciliationRule.EXACT);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
    }

    @Test
    void testReconcile_priceTolerance_withinThreshold() {
        ReconciliationEngine engine = new ReconciliationEngine();
        try {
            EquityTrade internal = equity("EQU-20260603-0002", "100.00", "10", 1L);
            EquityTrade external = equity("EQU-20260603-0002", "100.50", "10", 1L);

            List<ReconResult> results = engine.reconcile(List.of(internal), List.of(external),
                    ReconciliationRule.PRICE_TOLERANCE_1PCT);

            assertThat(results)
                    .singleElement()
                    .extracting(ReconResult::status)
                    .isEqualTo(ReconResult.Status.MATCHED);
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        ReconciliationEngine engine = new ReconciliationEngine();
        try {
            EquityTrade internal = equity("EQU-20260603-0003", "100.00", "10", 1L);

            List<ReconResult> results = engine.reconcile(List.of(internal), List.of(), ReconciliationRule.EXACT);

            assertThat(results)
                    .singleElement()
                    .satisfies(result -> {
                        assertThat(result.status()).isEqualTo(ReconResult.Status.BREAK);
                        assertThat(result.discrepancyType()).isEqualTo("MISSING_EXTERNAL");
                    });
        } finally {
            engine.shutdown();
        }
    }

   @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        assertThat(engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT)).isEmpty();
    }

    @Test
    void testReconcile_singleInternalNoExternal_returnsBreak() {
        EquityTrade internal = equity("EQU-20260603-0004", "100.00", "1000", 1L);

        List<ReconResult> out = engine.reconcile(List.of(internal), List.of(), ReconciliationRule.EXACT);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(out.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }

    @Test
    void testReconcile_allMismatched_summaryShowsZeroMatched() {
        List<TradeType> internals = List.of(
                equity("EQU-20260603-0005", "100.00", "1000", 1L),
                equity("EQU-20260603-0006", "100.00", "1000", 1L),
                equity("EQU-20260603-0007", "100.00", "1000", 1L));
        List<TradeType> externals = List.of(
                equity("EQU-20260603-0005", "200.00", "1000", 1L),
                equity("EQU-20260603-0006", "200.00", "1000", 1L),
                equity("EQU-20260603-0007", "200.00", "1000", 1L));

        List<ReconResult> out = engine.reconcile(internals, externals, ReconciliationRule.EXACT);
        ReconSummary summary = out.stream().collect(new ReconSummaryCollector());

        assertThat(summary.total()).isEqualTo(3);
        assertThat(summary.matched()).isEqualTo(0);
        assertThat(summary.broken()).isEqualTo(3);
        ReconciliationEngine engine = new ReconciliationEngine();
        try {
            assertThat(engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT)).isEmpty();
        } finally {
            engine.shutdown();
        }
    }

    @Test
    void testReconcileByCounterparty_mergesAllFutureResults() {
        ReconciliationEngine engine = new ReconciliationEngine();
        try {
            Map<Long, List<TradeType>> internalByCp = Map.of(
                    1L, List.of(equity("EQU-20260603-0010", "100.00", "10", 1L)),
                    2L, List.of(equity("EQU-20260603-0011", "200.00", "20", 2L))
            );
            Map<Long, List<TradeType>> externalByCp = Map.of(
                    1L, List.of(equity("EQU-20260603-0010", "100.00", "10", 1L)),
                    2L, List.of(equity("EQU-20260603-0011", "200.00", "20", 2L))
            );

            List<ReconResult> results = engine
                    .reconcileByCounterparty(internalByCp, externalByCp, ReconciliationRule.EXACT)
                    .join();

            assertThat(results).hasSize(2);
            assertThat(results).allMatch(result -> result.status() == ReconResult.Status.MATCHED);
        } finally {
            engine.shutdown();
        }
    }

    private EquityTrade equity(String ref, String price, String qty, long counterpartyId) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(counterpartyId)
                .build();
    }
}

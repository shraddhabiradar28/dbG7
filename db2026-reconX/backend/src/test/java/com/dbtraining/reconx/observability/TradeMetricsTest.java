package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TradeMetricsTest {

    @Test
    void incrementTradeCreated_incrementsTheRegisteredCounter() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TradeMetrics metrics = new TradeMetrics(registry, mock(ReconBreakRepository.class));

        metrics.incrementTradeCreated();
        metrics.incrementTradeCreated();

        assertThat(registry.get("trade_created_total").counter().count()).isEqualTo(2.0);
    }

    @Test
    void recordTradeValue_recordsOnTheDistributionSummary() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TradeMetrics metrics = new TradeMetrics(registry, mock(ReconBreakRepository.class));

        metrics.recordTradeValue(24550.0);

        assertThat(registry.get("trade_value_total").summary().totalAmount()).isEqualTo(24550.0);
    }
}

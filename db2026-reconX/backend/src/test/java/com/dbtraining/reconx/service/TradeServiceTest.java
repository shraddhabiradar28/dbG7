package com.dbtraining.reconx.service;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;

class TradeServiceTest {

    @Test
    void listDelegatesToRepositoryWithCombinedSpecifications() {
        TradeRepository tradeRepo = mock(TradeRepository.class);
        CounterpartyRepository counterpartyRepo = mock(CounterpartyRepository.class);
        InstrumentRepository instrumentRepo = mock(InstrumentRepository.class);
        TradeEventProducer events = mock(TradeEventProducer.class);
        TradeMetrics metrics = mock(TradeMetrics.class);
        TradeService service = new TradeService(tradeRepo, counterpartyRepo, instrumentRepo, events, metrics);

        Page<Trade> expected = new PageImpl<>(List.of(new Trade()));
        Pageable pageable = Pageable.unpaged();
        when(tradeRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(expected);

        Page<Trade> actual = service.list(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "NEW",
                42L,
                pageable);

        assertThat(actual).isSameAs(expected);
        verify(tradeRepo).findAll(any(Specification.class), eq(pageable));
    }
}

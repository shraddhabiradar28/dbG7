package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeLookupServiceTest {

    private final TradeRepository tradeRepository = mock(TradeRepository.class);
    private final CounterpartyRepository counterpartyRepository = mock(CounterpartyRepository.class);
    private final TradeLookupService service = new TradeLookupService(tradeRepository, counterpartyRepository);

    @Test
    void counterpartyForTradeRef_returnsResolvedCounterparty() {
        Counterparty counterparty = counterparty(7L, "DB AG");
        Trade trade = trade("EQU-20260603-0001", counterparty);

        when(tradeRepository.findByTradeRef("EQU-20260603-0001")).thenReturn(Optional.of(trade));
        when(counterpartyRepository.findById(7L)).thenReturn(Optional.of(counterparty));

        assertThat(service.counterpartyForTradeRef("EQU-20260603-0001")).isSameAs(counterparty);
    }

    @Test
    void counterpartyForTradeRef_missingTrade_throwsWithTradeRefInMessage() {
        when(tradeRepository.findByTradeRef("EQU-20260603-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.counterpartyForTradeRef("EQU-20260603-9999"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("EQU-20260603-9999");
    }

    private static Trade trade(String tradeRef, Counterparty counterparty) {
        Trade trade = new Trade();
        trade.setTradeRef(tradeRef);
        trade.setCounterparty(counterparty);
        return trade;
    }

    private static Counterparty counterparty(Long id, String name) {
        Counterparty counterparty = new Counterparty();
        setId(counterparty, id);
        counterparty.setName(name);
        counterparty.setLeiCode("LEI-" + id);
        counterparty.setRegion("EMEA");
        return counterparty;
    }

    private static void setId(Counterparty counterparty, Long id) {
        try {
            var field = Counterparty.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(counterparty, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}

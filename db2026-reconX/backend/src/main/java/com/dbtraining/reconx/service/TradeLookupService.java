package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class TradeLookupService {

    private final TradeRepository tradeRepository;
    private final CounterpartyRepository counterpartyRepository;

    public TradeLookupService(TradeRepository tradeRepository, CounterpartyRepository counterpartyRepository) {
        this.tradeRepository = tradeRepository;
        this.counterpartyRepository = counterpartyRepository;
    }

    public Counterparty counterpartyForTradeRef(String tradeRef) {
        return tradeRepository.findByTradeRef(tradeRef)
                .map(trade -> trade.getCounterparty().getId())
                .flatMap(counterpartyRepository::findById)
                .orElseThrow(() -> new NoSuchElementException("No counterparty resolvable for trade " + tradeRef));
    }
}

package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo,
                        TradeEventProducer events,
                        TradeMetrics metrics) {
    }
public Trade create(TradeRequest req, String actor) {

    // Check for duplicate trade reference
    tradeRepo.findByTradeRef(req.tradeRef())
            .ifPresent(t -> {
                throw new DuplicateTradeRefException(req.tradeRef());
            });

    // Find Instrument
    Instrument instrument = instRepo.findById(req.instrumentId())
            .orElseThrow(() ->
                    new TradeNotFoundException(String.valueOf(req.instrumentId())));

    // Find Counterparty
    Counterparty counterparty = cpRepo.findById(req.counterpartyId())
            .orElseThrow(() ->
                    new TradeNotFoundException(String.valueOf(req.counterpartyId())));

    // Create and populate Trade entity
    Trade trade = new Trade();
    trade.setTradeRef(req.tradeRef());
    trade.setInstrument(instrument);
    trade.setCounterparty(counterparty);
    trade.setAssetClass(req.assetClass());
    trade.setSide(req.side());
    trade.setQuantity(req.quantity());
    trade.setPrice(req.price());
    trade.setTradeDate(req.tradeDate());
    trade.setStatus("PENDING");

    // Save the trade
    Trade saved = tradeRepo.save(trade);

    // TICKET-ADV083 - Metrics
    metrics.incrementTradeCreated();

    metrics.recordTradeValue(
            saved.getQuantity()
                    .multiply(saved.getPrice())
                    .doubleValue()
    );

    // TICKET-ADV129 - Publish event
    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    saved.getTradeRef(),
                    TradeEvent.EventType.TRADE_CREATED,
                    Instant.now(),
                    actor,
                    null,
                    saved.getStatus()
            )
    );

    return saved;
}

    public Trade update(Long id, TradeRequest req, String actor) {

    // Load the existing trade
    Trade trade = tradeRepo.findById(id)
            .orElseThrow(() ->
                    new TradeNotFoundException("Trade not found: " + id));

    // Load Instrument
    Instrument instrument = instRepo.findById(req.instrumentId())
            .orElseThrow(() ->
                    new TradeNotFoundException("Instrument not found: " + req.instrumentId()));

    // Load Counterparty
    Counterparty counterparty = cpRepo.findById(req.counterpartyId())
            .orElseThrow(() ->
                    new TradeNotFoundException("Counterparty not found: " + req.counterpartyId()));

    // Update mutable fields
    trade.setTradeRef(req.tradeRef());
    trade.setInstrument(instrument);
    trade.setCounterparty(counterparty);
    trade.setAssetClass(req.assetClass());
    trade.setSide(req.side());
    trade.setQuantity(req.quantity());
    trade.setPrice(req.price());
    trade.setTradeDate(req.tradeDate());

    // Save updated trade
    Trade saved = tradeRepo.save(trade);


    return saved;
}

public Trade updateStatus(Long id, String status, String actor) {

    // Load the trade or throw if it doesn't exist
    Trade trade = tradeRepo.findById(id)
            .orElseThrow(() ->
                    new TradeNotFoundException(String.valueOf(id)));

    // Keep the old status for the event
    String previousStatus = trade.getStatus();

    // Update the status
    trade.setStatus(status);

    // Save the updated trade
    Trade saved = tradeRepo.save(trade);

    // Publish TRADE_UPDATED event
    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    saved.getTradeRef(),
                    TradeEvent.EventType.TRADE_UPDATED,
                    Instant.now(),
                    actor,
                    previousStatus,
                    saved.getStatus()
            )
    );

    return saved;
}

    public void softDelete(Long id, String actor) {
        // TODO(TICKET-ADV067): load, call t.softDelete() (sets deleted_at), save,
        //   publish a TRADE_CANCELLED event.
        throw new UnsupportedOperationException("TICKET-ADV067");
    }

    @Transactional(readOnly = true)
    public Page<Trade> list(LocalDate from, LocalDate to, String status, Long counterpartyId, Pageable pageable) {
        Specification<Trade> spec = Specification.where(tradeDateBetween(from, to))
                .and(hasStatus(status))
                .and(hasCounterparty(counterpartyId));
        return tradeRepo.findAll(spec, pageable);
    }
}

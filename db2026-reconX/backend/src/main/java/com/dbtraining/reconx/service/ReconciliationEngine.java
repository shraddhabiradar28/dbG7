package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.observability.ReconConfigMBean;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReconciliationEngine {
    private final ExecutorService executor;

    public ReconciliationEngine() {
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        AtomicInteger threadCounter = new AtomicInteger(1);
        this.executor = Executors.newFixedThreadPool(threads, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("recon-by-cp-" + threadCounter.getAndIncrement());
            return thread;
        });
    }

    private final ReconConfigMBean reconConfigMBean;

    public ReconciliationEngine(ReconConfigMBean reconConfigMBean) {
        this.reconConfigMBean = reconConfigMBean;
    }

    @Timed(value = "reconciliation.duration", description = "Wall time of reconcile()",
           percentiles = {0.5, 0.95, 0.99}, histogram = true)
    public List<ReconResult> reconcile(List<TradeType> internal,
                                       List<TradeType> external,
                                       ReconciliationRule rule) {
        if (internal == null || internal.isEmpty()) return List.of();

        Map<String, TradeType> externalByRef = (external == null ? List.<TradeType>of() : external)
                .stream()
                .collect(Collectors.toMap(t -> t.tradeRef().value(), Function.identity(), (a, b) -> a));

        return internal.parallelStream()
                .map(in -> matchOne(in, externalByRef.get(in.tradeRef().value()), rule))
                .toList();
    }

    private ReconResult matchOne(TradeType internal, TradeType external, ReconciliationRule rule) {
        String ref = internal.tradeRef().value();
        if (external == null) {
            return ReconResult.breakResult(ref, "MISSING_EXTERNAL",
                    "No external trade found for " + ref);
        }
        BigDecimal[] iPair = priceQty(internal);
        BigDecimal[] ePair = priceQty(external);
        if (rule.matches(iPair[0], iPair[1], ePair[0], ePair[1])) {
            return ReconResult.matched(ref);
        }
        return ReconResult.breakResult(ref, "VALUE_MISMATCH",
                "internal=%s/%s external=%s/%s".formatted(iPair[0], iPair[1], ePair[0], ePair[1]));
        double priceTolerance = reconConfigMBean.getPriceTolerance();
        // TODO(TICKET-ADV033): build a Map<tradeRef, TradeType> from `external`
        //   (O(1) lookups beat O(n*m) nested iteration), then parallelStream
        //   over `internal` and call matchOne(in, externalByRef.get(...), rule)
        //   for each. Guard against null/empty inputs (TICKET-ADV047).
        //   HINT:
        //     Map<String, TradeType> externalByRef = external.stream()
        //         .collect(Collectors.toMap(t -> t.tradeRef().value(), Function.identity(), (a, b) -> a));
        //     return internal.parallelStream()
        //         .map(in -> matchOne(in, externalByRef.get(in.tradeRef().value()), rule))
        //         .toList();
        throw new UnsupportedOperationException("TICKET-ADV033");
        if (internal == null || internal.isEmpty()) {
            return List.of();
        }

        Map<String, TradeType> externalByRef = (external == null ? List.<TradeType>of() : external)
                .stream()
                .collect(Collectors.toMap(
                        trade -> trade.tradeRef().value(),
                        Function.identity(),
                        (left, right) -> left
                ));

        return internal.parallelStream()
                .map(in -> matchOne(in, externalByRef.get(in.tradeRef().value()), rule))
                .toList();
    }

    /**
     * TICKET-ADV037 — split by counterparty, reconcile each batch concurrently,
     * combine into a single result list. Caller passes one external feed per
     * counterparty (typical real-world shape).
     */
    public CompletableFuture<List<ReconResult>> reconcileByCounterparty(
            Map<Long, List<TradeType>> internalByCp,
            Map<Long, List<TradeType>> externalByCp,
            ReconciliationRule rule) {
        if (internalByCp == null || internalByCp.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        Map<Long, List<TradeType>> safeExternalByCp = externalByCp == null ? Map.of() : externalByCp;

        List<CompletableFuture<List<ReconResult>>> futures = internalByCp.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(
                        () -> reconcile(entry.getValue(), safeExternalByCp.getOrDefault(entry.getKey(), List.of()), rule),
                        executor
                ))
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> futures.stream()
                        .flatMap(future -> future.join().stream())
                        .toList());
    }

    public void shutdown() {
        executor.shutdown();
    }

    private ReconResult matchOne(TradeType internal, TradeType external, ReconciliationRule rule) {
        double priceTolerance = reconConfigMBean.getPriceTolerance();
        // TODO(TICKET-ADV033): if external is null return ReconResult.breakResult(ref, "MISSING_EXTERNAL", ...).
        //   Otherwise pull priceQty() for both sides, compare via rule.matches(...),
        //   return ReconResult.matched(ref) or breakResult(ref, "VALUE_MISMATCH", details).
        throw new UnsupportedOperationException("TICKET-ADV033");
        String ref = internal.tradeRef().value();
        if (external == null) {
            return ReconResult.breakResult(ref, "MISSING_EXTERNAL", "No external trade found for " + ref);
        }

        BigDecimal[] internalPair = priceQty(internal);
        BigDecimal[] externalPair = priceQty(external);
        if (rule.matches(internalPair[0], internalPair[1], externalPair[0], externalPair[1])) {
            return ReconResult.matched(ref);
        }

        return ReconResult.breakResult(
                ref,
                "VALUE_MISMATCH",
                "internal=%s/%s external=%s/%s".formatted(
                        internalPair[0], internalPair[1], externalPair[0], externalPair[1]
                )
        );
    }

    private BigDecimal[] priceQty(TradeType t) {
        return switch (t) {
            case com.dbtraining.reconx.model.EquityTrade e     -> new BigDecimal[]{e.price(),  e.quantity()};
            case com.dbtraining.reconx.model.FXTrade fx        -> new BigDecimal[]{fx.fxRate(), fx.notionalCcy1()};
            case com.dbtraining.reconx.model.BondTrade b       -> new BigDecimal[]{b.couponRate(), b.faceValue()};
            case com.dbtraining.reconx.model.DerivativeTrade d -> new BigDecimal[]{d.strike(), d.quantity()};
            case com.dbtraining.reconx.model.EquityTrade equity ->
                    new BigDecimal[]{equity.price(), equity.quantity()};
            case com.dbtraining.reconx.model.FXTrade fx ->
                    new BigDecimal[]{fx.fxRate(), fx.notionalCcy1()};
            case com.dbtraining.reconx.model.BondTrade bond ->
                    new BigDecimal[]{bond.couponRate(), bond.faceValue()};
            case com.dbtraining.reconx.model.DerivativeTrade derivative ->
                    new BigDecimal[]{derivative.strike(), derivative.quantity()};
        };
    }
}
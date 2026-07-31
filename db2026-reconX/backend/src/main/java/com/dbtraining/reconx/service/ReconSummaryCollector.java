package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class ReconSummaryCollector
        implements Collector<ReconResult, ReconSummary.Builder, ReconSummary> {

    @Override
    public Supplier<ReconSummary.Builder> supplier() {
        return ReconSummary.Builder::new;
    }

    @Override
    public BiConsumer<ReconSummary.Builder, ReconResult> accumulator() {
        return (builder, result) -> {
            builder.total++;
            if (result.status() == ReconResult.Status.MATCHED) {
                builder.matched++;
            } else {
                builder.broken++;
            }
        };
    }

    @Override
    public BinaryOperator<ReconSummary.Builder> combiner() {
        return (left, right) -> {
            ReconSummary.Builder merged = new ReconSummary.Builder();
            merged.total = left.total + right.total;
            merged.matched = left.matched + right.matched;
            merged.broken = left.broken + right.broken;
            return merged;
        };
    }

    @Override
    public Function<ReconSummary.Builder, ReconSummary> finisher() {
        return builder -> new ReconSummary(builder.total, builder.matched, builder.broken);
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED);
    }
}

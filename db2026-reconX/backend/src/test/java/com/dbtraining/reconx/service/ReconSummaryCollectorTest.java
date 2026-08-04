package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ReconSummaryCollectorTest {

    @Test
    void emptyFactory_returnsZeroCounts() {
        assertThat(ReconSummary.empty()).isEqualTo(new ReconSummary(0, 0, 0));
    }

    @Test
    void collector_countsMatchedAndBrokenResults() {
        ReconSummary summary = List.of(
                ReconResult.matched("EQU-20260603-0001"),
                ReconResult.breakResult("EQU-20260603-0002", "VALUE_MISMATCH", "details"),
                ReconResult.matched("EQU-20260603-0003")
        ).stream().collect(new ReconSummaryCollector());

        assertThat(summary).isEqualTo(new ReconSummary(3, 2, 1));
    }

    @Test
    void parallelAndSerialCollects_produceSameSummary() {
        List<ReconResult> results = IntStream.range(0, 10_000)
                .mapToObj(i -> i % 3 == 0
                        ? ReconResult.breakResult("EQU-20260603-%04d".formatted(i), "VALUE_MISMATCH", "details")
                        : ReconResult.matched("EQU-20260603-%04d".formatted(i)))
                .toList();

        ReconSummary serial = results.stream().collect(new ReconSummaryCollector());
        ReconSummary parallel = results.parallelStream().collect(new ReconSummaryCollector());

        assertThat(parallel).isEqualTo(serial);
    }
}

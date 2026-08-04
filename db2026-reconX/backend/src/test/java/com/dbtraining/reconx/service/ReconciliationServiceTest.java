package com.dbtraining.reconx.service;

/*
 * ============================================================================
 * DISABLED — does not compile against current main.
 *
 * Introduced in commit 4e91f0a ("day 3 ticket 41 42 43") for a
 * ReconciliationService / ReconResultRepository pair that was never built —
 * neither class exists anywhere in the codebase. The architecture moved on
 * (ReconResult now lives in com.dbtraining.reconx.dto, Trade is a JPA entity
 * at com.dbtraining.reconx.repository.entity.Trade, and matching/persistence
 * is done directly by ReconciliationEngine + the controller/service layer),
 * so this Day 3 scaffold predates and no longer matches the design.
 *
 * Commented out rather than deleted so no work is lost.
 *
 * TO RE-ENABLE: either build the ReconciliationService/ReconResultRepository
 * this test expects, or rewrite it against the current ReconciliationEngine
 * API (see ReconciliationEngineTest for the current shape).
 * ============================================================================
 */

// import com.dbtraining.reconx.model.ReconResult;
// import com.dbtraining.reconx.model.Trade;
// import com.dbtraining.reconx.repository.ReconResultRepository;
// import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor;
//
// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.List;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.verify;
//
// class ReconciliationServiceTest {
//
//     @Test
//     void testReconcile_savesResultWithMatchedStatus() {
//         // given
//         ReconResultRepository repo = mock(ReconResultRepository.class);
//         ReconciliationEngine engine = new ReconciliationEngine();
//         ReconciliationService svc = new ReconciliationService(engine, repo);
//
//         Trade i = new Trade("TRD-1", "CP-1", "SAP.DE",
//                 new BigDecimal("10"), new BigDecimal("100"), LocalDate.now());
//         Trade e = new Trade("TRD-1", "CP-1", "SAP.DE",
//                 new BigDecimal("10"), new BigDecimal("100"), LocalDate.now());
//
//         // when
//         svc.runRecon(List.of(i), List.of(e));
//
//         // then
//         ArgumentCaptor<ReconResult> captor = ArgumentCaptor.forClass(ReconResult.class);
//         verify(repo).save(captor.capture());
//         assertThat(captor.getValue().tradeRef()).isEqualTo("TRD-1");
//         assertThat(captor.getValue().status()).isEqualTo(ReconResult.Status.MATCHED);
//     }
// }

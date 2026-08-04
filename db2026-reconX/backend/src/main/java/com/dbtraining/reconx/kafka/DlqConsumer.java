package com.dbtraining.reconx.kafka;

/*
 * ============================================================================
 * DISABLED — does not compile against current main.
 *
 * This consumer was pushed (commit e00fe38, shri-hari, 2026-08-03) without the
 * two types it depends on:
 *
 *   - com.dbtraining.reconx.model.DlqMessage
 *   - com.dbtraining.reconx.repository.DlqMessageRepository
 *
 * Neither exists anywhere in the repo or on any remote branch, so the backend
 * could not compile. Commented out rather than deleted so no work is lost.
 *
 * TO RE-ENABLE (@shri-hari): push DlqMessage (JPA entity with the builder used
 * below — eventId, tradeRef, originalTopic, partition, offset, payload, reason,
 * firstSeen), DlqMessageRepository, and the Liquibase changelog for its table.
 * Then uncomment this file.
 * ============================================================================
 */

// import com.dbtraining.reconx.dto.TradeEvent;
// import com.dbtraining.reconx.repository.DlqMessageRepository;
// import com.dbtraining.reconx.model.DlqMessage;
// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.kafka.support.KafkaHeaders;
// import org.springframework.messaging.handler.annotation.Header;
// import org.springframework.stereotype.Component;
//
// import java.time.Instant;
//
// @Component
// public class DlqConsumer {
//
//     private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);
//
//     private final DlqMessageRepository repo;
//
//     public DlqConsumer(DlqMessageRepository repo) {
//         this.repo = repo;
//     }
//
//     @KafkaListener(
//             topics = "trade-events-dlq",
//             groupId = "dlq-monitor",
//             containerFactory = "tradeEventListenerContainerFactory"
//     )
//     public void onDlqMessage(ConsumerRecord<String, TradeEvent> record,
//                              @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exMsg) {
//         TradeEvent event = record.value();
//         log.error("DLQ: trade={} eventId={} reason={}",
//                 event.tradeRef(), event.eventId(), exMsg);
//
//         repo.save(DlqMessage.builder()
//                 .eventId(event.eventId())
//                 .tradeRef(event.tradeRef())
//                 .originalTopic(record.topic().replace("-dlq", ""))
//                 .partition(record.partition())
//                 .offset(record.offset())
//                 .payload(event)
//                 .reason(exMsg)
//                 .firstSeen(Instant.now())
//                 .build());
//     }
// }

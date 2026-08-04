package com.dbtraining.reconx.kafka;

/*
 * ============================================================================
 * DISABLED — does not compile against current main.
 *
 * Pushed in commit ef12ef1 ("ticke 142-144", shraddhabiradar28, 2026-08-03)
 * against an API that does not exist:
 *
 *   - TradeEvent.created(String, ObjectNode)  — TradeEvent has no static
 *                                               factory methods at all
 *
 * This broke test-compile, so the whole backend build failed. Commented out
 * rather than deleted so no work is lost.
 *
 * TO RE-ENABLE (@shraddhabiradar28): add the TradeEvent.created factory that
 * tickets 142-144 were meant to introduce, then uncomment this file.
 * ============================================================================
 */

// import com.dbtraining.reconx.dto.TradeEvent;
// import com.dbtraining.reconx.repository.AuditLogRepository;
// import com.fasterxml.jackson.databind.node.JsonNodeFactory;
// import org.awaitility.Awaitility;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.KafkaContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;
// import org.testcontainers.utility.DockerImageName;
//
// import java.time.Duration;
// import java.util.stream.IntStream;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// @SpringBootTest
// @Testcontainers
// class KafkaPipelineIT {
//
//     @Container
//     static KafkaContainer kafka = new KafkaContainer(
//             DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
//     );
//
//     @DynamicPropertySource
//     static void kafkaProps(DynamicPropertyRegistry registry) {
//         registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
//     }
//
//     @Autowired TradeEventProducer producer;
//     @Autowired AuditLogRepository auditRepo;
//
//     @Test
//     void publishesAndConsumes100Events() {
//         long before = auditRepo.count();
//
//         IntStream.range(0, 100).forEach(i ->
//                 producer.publish(TradeEvent.created(
//                         "TRD-IT-" + i,
//                         JsonNodeFactory.instance.objectNode().put("price", i)
//                 ))
//         );
//
//         Awaitility.await()
//                 .atMost(Duration.ofSeconds(30))
//                 .pollInterval(Duration.ofMillis(500))
//                 .untilAsserted(() ->
//                         assertThat(auditRepo.count()).isEqualTo(before + 100)
//                 );
//     }
// }

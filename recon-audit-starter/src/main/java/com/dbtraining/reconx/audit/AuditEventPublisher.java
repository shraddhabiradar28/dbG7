package com.dbtraining.reconx.audit;

import org.springframework.context.ApplicationEventPublisher;

public class AuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public AuditEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(String eventType, String eventId) {
        applicationEventPublisher.publishEvent(new AuditEvent(eventType, eventId));
    }

    public record AuditEvent(String eventType, String eventId) {
    }
}

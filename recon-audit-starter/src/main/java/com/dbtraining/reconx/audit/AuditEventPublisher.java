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
public class AuditEventPublisher {

    private final AuditProperties properties;

    public AuditEventPublisher(AuditProperties properties) {
        this.properties = properties;
    }

    public void publish(String event) {
        System.out.println("[AuditEventPublisher] topic=" + properties.getTopic() + " event=" + event);
    }
}

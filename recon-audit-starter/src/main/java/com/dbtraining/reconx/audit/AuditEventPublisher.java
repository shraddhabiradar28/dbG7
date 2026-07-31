package com.dbtraining.reconx.audit;

public class AuditEventPublisher {

    private final AuditProperties properties;

    public AuditEventPublisher(AuditProperties properties) {
        this.properties = properties;
    }

    public void publish(String event) {
        System.out.println("[AuditEventPublisher] topic=" + properties.getTopic() + " event=" + event);
    }
}

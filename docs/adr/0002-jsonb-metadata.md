# ADR-0002 — Use JSONB for Instrument Metadata
- Status: Accepted
- Date: 2026-07-27
- Deciders: ReconX team

## Context
Our `instruments` table needs to store highly variable, asset-class-specific attributes. Adding concrete columns for every possible attribute would result in a sparse, overly wide table requiring constant schema migrations (DDL) whenever a new asset class is onboarded. 

## Decision
Store instrument-specific attributes in a `metadata` column using PostgreSQL's `JSONB` data type. 

*Alternatives considered:*
- Entity-Attribute-Value (EAV) pattern (rejected due to complex, slow JOINs).
- Text column with application-side JSON parsing (rejected because it prevents database-level indexing).

## Consequences
**Positive**
- Schema flexibility: New asset attributes can be added immediately without DDL changes.
- `JSONB` supports binary storage and indexing, making queries highly efficient.

**Negative**
- Data integrity: The database cannot natively enforce strict schemas inside the JSON object, shifting validation responsibility to the Spring Boot application layer.
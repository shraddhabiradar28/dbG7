# ADR-0001 — Partition the `trades` table by `trade_date`
- Status: Accepted
- Date: 2026-07-27
- Deciders: ReconX team

## Context
`trades` is our highest-volume table — ~50k inserts/day, 5-year retention = ~91M rows at steady state. The vast majority of queries (dashboards, recon runs, analyst lookups) filter by a date range. A single unpartitioned table forces full-table scans for date-range deletes and complicates archival of older trade data.

## Decision
Partition `trades` by RANGE on `trade_date`, with one partition per calendar month. The primary key includes `trade_date` to satisfy Postgres' partitioning constraint. Child partitions are named `trades_yYYYYmMM` and are pre-created for the next 12 months by a monthly maintenance job. 

A `trades_default` partition catches any out-of-range inserts so the table never rejects writes.

## Consequences
**Positive**
- Partition pruning eliminates 11/12 of the data on a typical month-filtered query.
- Archival becomes a DDL operation (`DETACH PARTITION`), not a row-level delete.

**Negative**
- Composite PK `(id, trade_date)` complicates JPA `@Id` mapping.
- Pre-creating partitions is a recurring ops task — must be automated.
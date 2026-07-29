# ADR-0003 — Use GIN `jsonb_path_ops` Index for Metadata
- Status: Accepted
- Date: 2026-07-27
- Deciders: ReconX team

## Context
Following ADR-0002, our `instruments` table uses a `JSONB` column for metadata. Recon analysts frequently need to filter instruments based on specific keys and values nested inside this JSON object. A standard B-tree index is ineffective here because it indexes the entire JSON document as a single value, leading to slow sequential scans.

## Decision
Create a Generalized Inverted Index (GIN) using the `jsonb_path_ops` operator class on the `instruments.metadata` column.

*Alternatives considered:*
- Standard B-tree (rejected as it cannot index internal keys/values).
- GIN with `jsonb_ops` (rejected because `jsonb_path_ops` is smaller and faster for our specific `@>` containment queries).

## Consequences
**Positive**
- Dramatic read performance improvements: Containment queries (using the `@>` operator) resolve via index rather than full table scans.
- `jsonb_path_ops` uses significantly less disk space than the default `jsonb_ops`.

**Negative**
- Write penalty: GIN indexes are slower to update than B-trees, slightly increasing the latency of `INSERT` operations.
-- ============================================================================
-- TICKET-ADV010 — VWAP per instrument per day (window function)
-- ============================================================================
SELECT DISTINCT
    t.instrument_id,
    t.trade_date,
    SUM(t.price * t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date)
        / NULLIF(SUM(t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date), 0)
            AS vwap
FROM trades t
WHERE t.deleted_at IS NULL
  AND t.asset_class = 'EQUITY'
ORDER BY t.trade_date DESC, t.instrument_id;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle (execution -> settlement
--                -> recon_break -> resolution)
-- ============================================================================
WITH RECURSIVE trade_lifecycle AS (
    -- anchor: every trade in its execution state
    SELECT
        t.id           AS trade_id,
        t.trade_ref,
        1              AS step,
        'EXECUTED'     AS state,
        t.created_at   AS at_ts,
        NULL::text     AS detail
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    -- recursive: each subsequent state derived from the previous step
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.step + 1,
        CASE tl.step
            WHEN 1 THEN 'CONFIRMED'
            WHEN 2 THEN 'SETTLED'
            WHEN 3 THEN 'RECONCILED'
        END                                          AS state,
        s.settlement_date::timestamp                  AS at_ts,
        s.status                                      AS detail
    FROM trade_lifecycle tl
    -- JOIN LATERAL (
    --     SELECT 
    --        2 AS Step,
    --        'CONFIRMATION' AS state,
    --        c.confirmed_at AS at_ts,
    --        c.status AS detail
    --     FROM confirmation c 
    --     WHERE t1.step = 1
    --         AND c.trade_id = t1.rade_id
        
    --     UNION ALL

    --     SELECT 
    --         3,
    --         'SETTLEMENT',
    --         s.settlement_date,
    --         s.status
    --     FROM settlements s
    --     WHERE t1.step=2
    --        AND s.trade_id=t1.trade_id
        
    --     UNION ALL
    --     SELECT
    --       3,
    --       'SETTLEMENT',
    --        s.settlement_date,
    --        s.status
    --     FROM settlements s
    --     WHERE t1.step = 2
    --         AND s.trade_id =t1.trade_id

    --     UNION ALL

    --     SELECT
    --         4,
    --         'RECON_BREAK',
    --         rb.created_at,
    --         rb.reason

    --     FROM recon_breaks rb
    --     WHERE t1.step =3
    --         AND rb.trade_id=t1.trade_id

    --     UNION ALL
    --     SELECT
    --         5,
    --         'RESOLUTION',
    --         r.resolved_at,
    --         r.after_state
    --     FROM resolutions r
    --     WHERE t1.step =4
    --         AND r.trade_id=t1.trade_id    
    -- ) nxt on TRUE
    JOIN settlements s ON s.trade_id = tl.trade_id
    WHERE tl.step < 4
)
SELECT * FROM trade_lifecycle
ORDER BY trade_id, step;


-- ============================================================================
-- ADV008 — REFRESH the daily-summary materialised view (concurrent so it can
--         run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB lookup: which instruments have sector = 'Banking'?
-- ============================================================================
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"sector":"Banking"}'::jsonb;

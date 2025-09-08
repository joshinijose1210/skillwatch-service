SELECT
    NOT EXISTS (
        SELECT 1
        FROM kra
        LEFT JOIN kra_kpi_mapping kkm ON kra.id = kkm.kra_id
        LEFT JOIN kpi ON kkm.kpi_id = kpi.id AND kpi.status = true
        WHERE kra.organisation_id = :organisationId
        GROUP BY kra.id
        HAVING COUNT(kpi.id) = 0
    ) AS all_kras_has_active_kpis;
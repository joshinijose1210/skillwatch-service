SELECT
    NOT EXISTS (
        -- Find designations missing active KPIs for any KRA
        SELECT 1
        FROM designations d
        CROSS JOIN kra k
        LEFT JOIN kpi_department_team_designation_mapping kdtm
            ON kdtm.designation_id = d.id
        LEFT JOIN kpi
            ON kpi.id = kdtm.kpi_id
            AND kpi.status = true -- Ensure KPI is active
        LEFT JOIN kra_kpi_mapping kkm
            ON kkm.kpi_id = kpi.id
            AND kkm.kra_id = k.id -- Ensure KPI is mapped to the correct KRA
        WHERE d.organisation_id = :organisationId
          AND d.status = true -- Only active designations
          AND k.organisation_id = :organisationId
        GROUP BY d.id, k.id
        HAVING COUNT(kkm.kpi_id) = 0 -- Ensure active KPI is mapped to the current KRA
    ) AS all_designations_have_active_kpis_for_kras;

-- This query checks whether any employee assigned a given KPI
-- has started their review for the specified review cycle.
SELECT EXISTS (
    SELECT 1
    FROM kpi_department_team_designation_mapping kdtdm
    JOIN employees_designation_mapping edm ON kdtdm.designation_id = edm.designation_id
    JOIN review_details rd ON edm.emp_id = rd.review_to
    WHERE rd.review_cycle_id = :reviewCycleId
    AND kdtdm.kpi_id = :kpiId
) AS has_started_review;

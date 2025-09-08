SELECT
  kpi.organisation_id,
  kpi.id,
  kpi.kpi_id,
  kpi.title,
  kpi.description,
  kpi_version_mapping.version_number,
  COALESCE(kra.id, null) AS kra_id,
  COALESCE(kra.name, null) AS kra_name
FROM
  kpi
  JOIN kpi_version_mapping ON kpi.id = kpi_version_mapping.kpi_id
  JOIN kpi_department_team_designation_mapping ON kpi.id = kpi_department_team_designation_mapping.kpi_id
  JOIN employees_team_mapping_view ON employees_team_mapping_view.team_id = kpi_department_team_designation_mapping.team_id
  JOIN employees_designation_mapping ON employees_designation_mapping.designation_id = kpi_department_team_designation_mapping.designation_id
  LEFT JOIN kra_kpi_mapping ON kpi.id = kra_kpi_mapping.kpi_id
  LEFT JOIN kra ON kra.id = kra_kpi_mapping.kra_id
WHERE
  employees_team_mapping_view.emp_id = :reviewToId
  AND employees_designation_mapping.emp_id = :reviewToId
  AND kpi.organisation_id = :organisationId
  AND kpi.status = true;
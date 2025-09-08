SELECT
  kpi.id,
  kpi.title,
  kpi.description,
  kpi.status,
  kpi.kpi_id as display_id,
  kpi.organisation_id,
  kpi_version_mapping.version_number,
  COALESCE(kra.id, null) AS kra_id
FROM
  kpi
  JOIN kpi_version_mapping ON kpi.id = kpi_version_mapping.kpi_id
  LEFT JOIN kra_kpi_mapping ON kpi.id = kra_kpi_mapping.kpi_id
  LEFT JOIN kra ON kra.id = kra_kpi_mapping.kra_id
WHERE
  kpi.id = :id;
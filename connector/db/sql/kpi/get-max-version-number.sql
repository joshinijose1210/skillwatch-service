SELECT
  MAX(version_number) AS version_number
FROM
  kpi
  JOIN kpi_version_mapping ON kpi.id = kpi_version_mapping.kpi_id
WHERE
  kpi.kpi_id = :kpiId;
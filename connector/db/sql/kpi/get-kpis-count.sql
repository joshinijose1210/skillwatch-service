SELECT COUNT(DISTINCT kpi.id) as kpi_count
FROM
  kpi
  JOIN (SELECT max(kpi.id) as id, kpi.kpi_id, max(kpi_version_mapping.version_number) as version_number, kpi.organisation_id, bool_or(kpi.status) as status
        FROM kpi
        JOIN kpi_version_mapping ON kpi.id = kpi_version_mapping.kpi_id
        WHERE kpi.organisation_id = :organisationId
        GROUP BY kpi.kpi_id, kpi.organisation_id) as kpi_data ON kpi_data.id = kpi.id
  LEFT JOIN kpi_department_team_designation_mapping ON kpi.id = kpi_department_team_designation_mapping.kpi_id
  LEFT JOIN kra_kpi_mapping ON kpi.id = kra_kpi_mapping.kpi_id
  LEFT JOIN kra ON kra.id = kra_kpi_mapping.kra_id
WHERE
  kpi.organisation_id = :organisationId
  AND (
    cast(:search as text) IS NULL
    OR UPPER(kpi.title) LIKE '%' || UPPER(:search) || '%'
    OR UPPER(kpi.description) LIKE '%' || UPPER(:search) || '%'
  )
  AND (:departmentId::INT[] = '{-99}' OR kpi_department_team_designation_mapping.department_id = ANY (:departmentId::INT[]))
  AND (:teamId::INT[] = '{-99}' OR kpi_department_team_designation_mapping.team_id = ANY (:teamId::INT[]))
  AND (:designationId::INT[] = '{-99}' OR kpi_department_team_designation_mapping.designation_id = ANY (:designationId::INT[]))
  AND (:kraId::INT[] = '{-99}' OR kra_kpi_mapping.kra_id = ANY (:kraId::INT[]))
  AND (:status::BOOL[] = '{true,false}' OR kpi_data.status = ANY (:status::BOOL[]));

SELECT
  COALESCE(kdtdm.department_id, null) AS department_id,
  COALESCE(kdtdm.team_id, null)AS team_id,
  COALESCE(kdtdm.designation_id, null) AS designation_id,
  COALESCE(dep.department_name, null) AS department_name,
  COALESCE(t.team_name, null) AS team_name,
  COALESCE(d.designation_name, null) AS designation_name
FROM
  kpi_department_team_designation_mapping kdtdm
  LEFT JOIN departments dep ON dep.id = kdtdm.department_id AND dep.status = true
  LEFT JOIN teams t ON t.id = kdtdm.team_id AND t.status = true
  LEFT JOIN designations d ON d.id = kdtdm.designation_id AND d.status = true
WHERE
  kpi_id =:kpiId;


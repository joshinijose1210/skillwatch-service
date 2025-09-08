SELECT
  teams.id,
  teams.organisation_id,
  teams.team_id,
  COALESCE(department_team_mapping.department_id, null) AS department_id,
  COALESCE(departments.department_name, null) AS department_name,
  COALESCE(departments.department_id, null) AS department_display_id,
  COALESCE(departments.status, null) AS department_status,
  teams.team_name,
  teams.created_at,
  teams.updated_at,
  teams.status
FROM
  teams
  LEFT JOIN department_team_mapping ON department_team_mapping.team_id = teams.id
  LEFT JOIN departments ON departments.id = department_team_mapping.department_id
WHERE teams.organisation_id = :organisationId
    AND (cast(:searchText as text) IS NULL
    OR UPPER(teams.team_name) LIKE UPPER(:searchText)
    OR UPPER(departments.department_name) LIKE UPPER(:searchText))
    AND (:departmentId::INT[] = '{-99}' OR department_team_mapping.department_id = ANY (:departmentId::INT[]))
ORDER BY
  teams.created_at DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);



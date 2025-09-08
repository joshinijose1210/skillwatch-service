SELECT
  teams.organisation_id,
  teams.id,
  teams.team_id as display_id,
  teams.team_name,
  teams.status,
  COALESCE(department_team_mapping.department_id, null) AS department_id,
  COALESCE(departments.department_name, null) AS department_name,
  COALESCE(departments.department_id, null) AS department_display_id,
  COALESCE(departments.status, null) AS department_status,
  teams.created_at,
  teams.updated_at
FROM
  teams
  LEFT JOIN department_team_mapping ON department_team_mapping.team_id = teams.id
  LEFT JOIN departments ON departments.id = department_team_mapping.department_id
WHERE
  teams.id = :id
  AND teams.organisation_id = :organisationId ;

SELECT COUNT(teams.team_id) AS unlinked_team_count
FROM teams
  LEFT JOIN department_team_mapping ON department_team_mapping.team_id = teams.id
  LEFT JOIN departments ON departments.id = department_team_mapping.department_id
WHERE teams.organisation_id = :organisationId
  AND department_team_mapping.team_id IS NULL
  AND teams.team_id != 1 ;
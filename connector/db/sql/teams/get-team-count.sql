SELECT COUNT(teams.team_id) AS team_count
FROM
  teams
  LEFT JOIN department_team_mapping ON department_team_mapping.team_id = teams.id
  LEFT JOIN departments ON departments.id = department_team_mapping.department_id
WHERE teams.organisation_id = :organisationId
    AND (cast(:searchText as text) IS NULL
    OR UPPER(teams.team_name) LIKE UPPER(:searchText)
    OR UPPER(departments.department_name) LIKE UPPER(:searchText))
    AND (:departmentId::INT[] = '{-99}' OR department_team_mapping.department_id = ANY (:departmentId::INT[]));
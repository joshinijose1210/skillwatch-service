WITH subquery AS (
    SELECT status
    FROM teams
    JOIN department_team_mapping ON teams.id = department_team_mapping.team_id
    WHERE
    department_team_mapping.department_id = :departmentId
    AND organisation_id = :organisationId
    AND LOWER(team_name) = LOWER(:teamName)
)
SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery);
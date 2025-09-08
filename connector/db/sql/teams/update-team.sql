UPDATE
  teams
SET
  team_name = :teamName,
  status = :teamStatus,
  updated_at = CURRENT_TIMESTAMP
WHERE
  organisation_id = :organisationId
  AND id = :id ;


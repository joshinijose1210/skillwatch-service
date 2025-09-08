INSERT INTO teams(organisation_id, team_id, team_name, status)
VALUES
  (:organisationId, :id, :teamName, :teamStatus)RETURNING *;


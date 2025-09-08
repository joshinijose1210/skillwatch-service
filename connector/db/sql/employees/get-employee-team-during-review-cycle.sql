SELECT
    rc.id AS review_cycle_id,
    etm.id,
    etm.team_id,
    t.team_name
FROM
    review_cycle rc
JOIN employees_team_mapping etm
ON etm.emp_id = :employeeId
AND etm.joined_at <= rc.end_date
AND (etm.left_at IS NULL OR etm.left_at >= rc.start_date)
JOIN teams t ON etm.team_id = t.id
WHERE rc.organisation_id = :organisationId
  AND rc.id = :reviewCycleId
ORDER BY etm.id
DESC LIMIT 1;

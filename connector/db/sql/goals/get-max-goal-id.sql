SELECT MAX(goal_id) as max_id FROM goals g WHERE g.organisation_id = :organisationId;

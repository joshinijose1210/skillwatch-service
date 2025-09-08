INSERT INTO employees_team_mapping(emp_id,team_id, joined_at, is_active)
VALUES (
  :id,
  :team_id,
  :joinedAt,
  true
  ) RETURNING *;

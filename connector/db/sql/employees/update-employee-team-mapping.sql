UPDATE employees_team_mapping etm
SET left_at = :leftAt,
    is_active = false
WHERE emp_id = :id
AND is_active = true;
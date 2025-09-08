SELECT
  user_activity.activity,
  user_activity.created_at,
  employees.first_name,
  employees.last_name,
  employees.emp_id
FROM
  user_activity
  JOIN employees ON user_activity.employee_id = employees.id
WHERE
  employees.organisation_id = :organisationId
ORDER BY
 user_activity.created_at DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);
SELECT COUNT(user_activity.activity) AS user_activities_count
FROM
  user_activity
  JOIN employees ON user_activity.employee_id = employees.id
WHERE
  employees.organisation_id = :organisationId;
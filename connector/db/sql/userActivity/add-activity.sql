INSERT INTO user_activity(
  employee_id,
  module_id,
  activity,
  description,
  ip_address
)
VALUES
  (
    :employeeId,
    :moduleId,
    :activity,
    :description,
    :ipAddress
  ) ;
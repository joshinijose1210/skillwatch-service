SELECT
  password
FROM
  employees
WHERE
  LOWER(email_id) = LOWER(:emailId);
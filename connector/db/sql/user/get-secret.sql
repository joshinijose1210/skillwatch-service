SELECT
  password
FROM
  users
WHERE
  LOWER(email_id) = LOWER(:emailId);
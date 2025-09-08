UPDATE
  employees
SET
  password = :password
WHERE
  email_id = :email_id
  AND status = true;
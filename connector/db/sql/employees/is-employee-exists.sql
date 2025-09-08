SELECT EXISTS (
  SELECT 1 FROM employees WHERE
      LOWER(email_id) = LOWER(:emailId)
      AND status = true
);

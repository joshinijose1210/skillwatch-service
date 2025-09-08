SELECT EXISTS (
  SELECT 1 FROM users WHERE
      LOWER(email_id) = LOWER(:emailId)
      AND is_super_admin = true
) ;
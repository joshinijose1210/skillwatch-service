SELECT is_active
FROM organisations
INNER JOIN employees ON organisations.sr_no = employees.organisation_id
WHERE LOWER(employees.email_id) = LOWER(:emailId);

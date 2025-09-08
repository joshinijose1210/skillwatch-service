SELECT
    users.first_name,
    users.last_name,
    users.email_id,
    users.created_at::DATE,
    COALESCE(organisations.sr_no, null) as organisation_id,
    COALESCE(organisations.organisation_size, null) as organisation_size,
    COALESCE(organisations.name, null) as organisation_name,
    COALESCE(organisations.time_zone, null) as organisation_timezone,
    COALESCE(employees.contact_no, null) as contact_no
FROM users
LEFT JOIN employees ON users.email_id = employees.email_id
LEFT JOIN organisations on employees.id = organisations.admin_id
WHERE users.is_org_admin = TRUE
ORDER BY users.id DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);

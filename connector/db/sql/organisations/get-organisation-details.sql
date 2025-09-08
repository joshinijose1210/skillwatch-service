SELECT
  organisations.sr_no AS id,
  organisations.name,
  organisations.organisation_size,
  org_admin.contact_no,
  organisations.time_zone,
  COUNT(CASE WHEN employees.status = true THEN 1 END) AS activeUsers,
  COUNT(CASE WHEN employees.status = false THEN 1 END) AS inactiveUsers
FROM
  organisations
  LEFT JOIN employees ON employees.organisation_id = organisations.sr_no
  LEFT JOIN employees as org_admin ON org_admin.id = organisations.admin_id
WHERE
  organisations.sr_no = :organisation_id
GROUP BY
  organisations.sr_no,
  org_admin.contact_no;

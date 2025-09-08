SELECT
  organisations.sr_no as id,
  organisations.admin_id as admin_id,
  organisations.name as organisation_name,
  organisations.organisation_size,
  organisations.time_zone
FROM
  organisations
WHERE
  organisations.is_active = TRUE;

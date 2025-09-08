SELECT
  name AS organisation_name
FROM
  organisations
WHERE
  sr_no = :organisationId;

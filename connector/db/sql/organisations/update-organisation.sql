UPDATE organisations
SET
  name = :organisationName,
  time_zone = :timeZone
WHERE sr_no = :id;

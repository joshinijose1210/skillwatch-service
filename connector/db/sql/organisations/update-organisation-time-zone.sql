UPDATE organisations
SET
  time_zone = :timeZone
WHERE sr_no = :organisationId;

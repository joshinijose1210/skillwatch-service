INSERT INTO organisations(
  name, is_active, organisation_size, time_zone
)
VALUES(
  :organisationName, :isActive, :organisationSize, :timeZone
) RETURNING sr_no;
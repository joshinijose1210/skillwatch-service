UPDATE
  kpi
SET
  status = :status
WHERE
  kpi.organisation_id = :organisationId
  AND id = :id;

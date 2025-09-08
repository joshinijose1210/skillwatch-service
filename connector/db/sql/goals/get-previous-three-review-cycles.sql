SELECT id, start_date, end_date
FROM review_cycle
WHERE organisation_id = :organisationId
  AND start_date < (
    SELECT start_date FROM review_cycle WHERE id = :currentCycleId
  )
ORDER BY start_date DESC
LIMIT 3;
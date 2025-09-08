SELECT id
FROM review_cycle
WHERE organisation_id = :organisationId
  AND end_date < CURRENT_DATE
ORDER BY end_date DESC
LIMIT 1;
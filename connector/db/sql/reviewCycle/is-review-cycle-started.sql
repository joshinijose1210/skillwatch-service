WITH subquery AS (
    SELECT id
    FROM review_cycle
    WHERE start_date = :todayDate
    AND publish = true
    AND organisation_id = :organisationId
)
SELECT EXISTS (SELECT 1 FROM subquery), (SELECT id FROM subquery);
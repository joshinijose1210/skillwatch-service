WITH subquery AS (
    SELECT id
    FROM review_cycle
    WHERE :currentDate BETWEEN manager_review_start_date AND check_in_end_date
    AND publish = true
    AND organisation_id = :organisationId
)
SELECT EXISTS (SELECT 1 FROM subquery);
SELECT
    rc.id,
    rc.start_date,
    rc.end_date
FROM
    review_cycle rc
JOIN
    review_cycle current_cycle
    ON current_cycle.id = :currentCycleId
WHERE
    rc.organisation_id = :organisationId
    AND rc.start_date < current_cycle.start_date
ORDER BY
    rc.start_date DESC
LIMIT
    :numberOfCycles;
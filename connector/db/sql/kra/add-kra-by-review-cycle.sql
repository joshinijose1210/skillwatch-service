INSERT INTO review_cycle_kra (review_cycle_id, kra_id, kra_name, kra_weightage)
SELECT
    :reviewCycleId AS review_cycle_id,
    k.id AS kra_id,
    k.name AS kra_name,
    k.weightage AS kra_weightage
FROM
    kra k
WHERE
    k.organisation_id = :organisationId AND
    NOT EXISTS (
        SELECT 1
        FROM review_cycle_kra rck
        WHERE rck.review_cycle_id = :reviewCycleId
    );

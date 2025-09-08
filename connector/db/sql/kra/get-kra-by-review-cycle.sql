SELECT
    kra.id,
    kra.sr_no,
    rck.kra_name AS kra_name,
    rck.kra_weightage AS kra_weightage,
    kra.organisation_id
FROM kra
JOIN review_cycle_kra AS rck
    ON rck.kra_id = kra.id AND rck.review_cycle_id = :reviewCycleId ;

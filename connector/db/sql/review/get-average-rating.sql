SELECT
    review_details.average_rating
FROM
    review_details
WHERE
    review_details.id = :reviewDetailsId
    AND review_details.review_cycle_id = :reviewCycleId ;

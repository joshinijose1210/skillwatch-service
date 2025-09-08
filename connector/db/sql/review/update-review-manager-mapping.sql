UPDATE
  review_manager_mapping
SET
  first_manager_id = :firstManagerId,
  second_manager_id = :secondManagerId
WHERE
  review_details_id = :reviewDetailsId;
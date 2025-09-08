UPDATE
  review_details
SET
  updated_at = CURRENT_TIMESTAMP,
  draft = :draft,
  published = :published,
  average_rating = :averageRating
WHERE
  review_details.review_type_id = :reviewTypeId
  AND review_details.id = :reviewDetailsId
  AND review_details.review_cycle_id = :reviewCycleId
  AND review_details.review_to = :reviewToId
  AND review_details.review_from = :reviewFromId
  AND review_details.draft = true RETURNING *;

UPDATE
  reviews
SET
  review = :review,
  rating = :rating
WHERE
  reviews.review_details_id = :reviewDetailsId
  AND reviews.kpi_id = :kpiId;

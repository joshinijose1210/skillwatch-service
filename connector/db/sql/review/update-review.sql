UPDATE
  reviews
SET
  review = :review,
  rating = :rating
WHERE
  reviews.id = :reviewId;

INSERT INTO review_details(
    review_cycle_id, review_to, review_from, draft, published, review_type_id, average_rating
)
VALUES(
    :reviewCycleId, :reviewToId, :reviewFromId, :draft, :published, :reviewTypeId, :averageRating
) RETURNING * ;

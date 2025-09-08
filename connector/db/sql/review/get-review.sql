SELECT
  reviews.id AS review_id,
  reviews.kpi_id,
  kpi.title,
  kpi.description,
  reviews.review,
  reviews.rating,
  COALESCE(kkm.kra_id, null) AS kra_id,
  COALESCE(kra.name, null) As kra_name
FROM
  reviews
  INNER JOIN kpi ON reviews.kpi_id = kpi.id
  LEFT JOIN kra_kpi_mapping AS kkm ON kkm.kpi_id = reviews.kpi_id
  LEFT JOIN kra ON kra.id = kkm.kra_id
WHERE
  reviews.id = :reviewId;
SELECT
  review_details.id AS review_details_id,
  review_details.review_cycle_id,
  review_details.review_to AS review_to_id,
  reviewToEmployee.emp_id AS review_to_employee_id,
  review_details.review_from AS review_from_id,
  reviewFromEmployee.emp_id AS review_from_employee_id,
  review_details.draft,
  review_details.published,
  review_details.review_type_id,
  review_details.updated_at
FROM
  review_details
  JOIN employees AS reviewToEmployee ON reviewToEmployee.id = review_details.review_to
  JOIN employees AS reviewFromEmployee ON reviewFromEmployee.id = review_details.review_from
WHERE
  review_details.review_type_id = ANY (:reviewTypeId::INT[])
  AND review_details.review_cycle_id = :reviewCycleId
  AND review_details.review_to = :reviewToId
  AND review_details.review_from = ANY (:reviewFromId::INT[]);
SELECT
  review.review_cycle_id,
  review.first_name,
  review.start_date,
  review.end_date,
  review.self_review_start_date,
  review.self_review_end_date,
  review.selfReviewDraft,
  review.selfReviewPublish,
  COUNT(CASE WHEN feedback_type_id = 1 THEN feedback_type_id END) AS Positive,
  COUNT(CASE WHEN feedback_type_id = 2 THEN feedback_type_id END) AS Improvement,
  COUNT(CASE WHEN feedback_type_id = 3 THEN feedback_type_id END) AS Appreciation
FROM
  (SELECT
     employees.id,
     employees.emp_id,
     employees.first_name,
     review_cycle.id AS review_cycle_id,
     review_cycle.start_date ,
     review_cycle.end_date ,
     review_cycle.self_review_start_date,
     review_cycle.self_review_end_date,
     review_details.draft AS selfReviewDraft,
     review_details.published AS selfReviewPublish
  FROM
    employees
    INNER JOIN review_cycle ON employees.organisation_id = review_cycle.organisation_id
    LEFT JOIN review_details ON review_details.review_cycle_id = review_cycle.id
    AND review_details.review_to = employees.id
    AND review_details.review_type_id = 1
  WHERE
    review_cycle.publish = true
    AND employees.organisation_id = :organisationId
    AND employees.id = :id
  ) AS review
  LEFT JOIN feedbacks ON review.id = feedbacks.feedback_to
  AND feedbacks.is_draft = false
  AND feedbacks.updated_at BETWEEN review.start_date AND review.end_date
GROUP BY
  review.review_cycle_id,
  review.first_name,
  review.start_date,
  review.end_date,
  review.self_review_start_date,
  review.self_review_end_date,
  review.selfReviewDraft,
  review.selfReviewPublish;
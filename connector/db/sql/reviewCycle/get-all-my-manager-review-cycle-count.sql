SELECT COUNT(start_date) AS manager_review_cycle_count
FROM (
SELECT
   review_cycle.id AS review_cycle_id,
   review_cycle.start_date,
   review_cycle.end_date,
   review_cycle.manager_review_start_date,
   review_cycle.manager_review_end_date,
   review_cycle.publish,
   COALESCE(employees.id, null) AS review_to_id,
   COALESCE(employees.emp_id, null) AS review_to_employee_id,
   COALESCE(employees.first_name, null) AS first_name,
   COALESCE(employees.last_name, null) AS last_name,
   COALESCE(manager_details.id, null) AS review_from_id,
   COALESCE(manager_details.emp_id, null) AS review_from_employee_id,
   COALESCE(manager_details.first_name, null) AS manager_first_name,
   COALESCE(manager_details.last_name, null) AS manager_last_name,
   COALESCE(teams.team_name, null) AS team_name,
   null AS draft,
   null AS published,
   null AS average_rating
FROM
  review_cycle
  LEFT JOIN employees ON employees.organisation_id = review_cycle.organisation_id
  LEFT JOIN employee_manager_mapping_view ON employee_manager_mapping_view.emp_id = employees.id
  LEFT JOIN employees AS manager_details ON manager_details.id = employee_manager_mapping_view.manager_id
  LEFT JOIN employees_team_mapping ON employees.id = employees_team_mapping.emp_id
  LEFT JOIN teams ON teams.id = employees_team_mapping.team_id
  LEFT JOIN review_details ON review_details.review_cycle_id = review_cycle.id
  AND review_details.review_to = employees.id
WHERE
  review_details.review_cycle_id IS NULL
  AND review_cycle.organisation_id = :organisationId
  AND employees.id = :reviewToId
  AND employees.status = true
  AND employees.created_at::date <= review_cycle.end_date
  AND (:reviewCycleId::INT[] = '{-99}' OR review_cycle.id = ANY (:reviewCycleId::INT[]))
  AND (:reviewFromId::INT[] = '{-99}' OR manager_details.id = ANY (:reviewFromId::INT[]))
UNION
SELECT
   review_cycle.id AS review_cycle_id,
   review_cycle.start_date,
   review_cycle.end_date,
   review_cycle.manager_review_start_date,
   review_cycle.manager_review_end_date,
   review_cycle.publish,
   COALESCE(employees.id, null) AS review_to_id,
   COALESCE(employees.emp_id, null) AS review_to_employee_id,
   COALESCE(employees.first_name, null) AS first_name,
   COALESCE(employees.last_name, null) AS last_name,
   COALESCE(manager_details.id, null) AS review_from_id,
   COALESCE(manager_details.emp_id, null) AS review_from_employee_id,
   COALESCE(manager_details.first_name, null) AS manager_first_name,
   COALESCE(manager_details.last_name, null) AS manager_last_name,
   COALESCE(teams.team_name, null) AS team_name,
   COALESCE(review_details.draft, null) AS draft,
   COALESCE(review_details.published, null) AS published,
   COALESCE(review_details.average_rating, null) AS average_rating
FROM
  review_cycle
  LEFT JOIN employees ON employees.organisation_id = review_cycle.organisation_id
  JOIN review_details ON review_details.review_cycle_id = review_cycle.id AND review_details.review_to = employees.id
  AND review_details.review_type_id = :reviewTypeId
  LEFT JOIN employees AS manager_details ON manager_details.id = review_details.review_from
  LEFT JOIN employees_team_mapping ON employees.id = employees_team_mapping.emp_id
  LEFT JOIN teams ON teams.id = employees_team_mapping.team_id
WHERE
  review_cycle.organisation_id = :organisationId
  AND employees.id = :reviewToId
  AND employees.status = true
  AND employees.created_at::date <= review_cycle.end_date
  AND (:reviewCycleId::INT[] = '{-99}' OR review_details.review_cycle_id = ANY (:reviewCycleId::INT[]))
  AND (:reviewFromId::INT[] = '{-99}' OR manager_details.id = ANY (:reviewFromId::INT[]))
  )
  AS my_manager_data;
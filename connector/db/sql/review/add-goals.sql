INSERT INTO goals(
  review_details_id, description, target_date, goal_id, type_id, assigned_to, created_by, organisation_id
)
VALUES(
   :reviewDetailsId, :description, :targetDate, :goalId, :typeId, :assignedTo, :createdBy, :organisationId
);

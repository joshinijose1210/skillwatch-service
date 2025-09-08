UPDATE
  link_details
SET
  no_of_hit = :noOfHit
WHERE
  link_details.id = :linkId
  RETURNING * ;

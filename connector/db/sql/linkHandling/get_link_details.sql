SELECT
  la.id,
  la.generation_time,
  la.no_of_hit,
  la.purpose
FROM
  link_details la
WHERE
  la.id = :linkId

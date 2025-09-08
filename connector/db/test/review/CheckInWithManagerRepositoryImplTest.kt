package review

import io.kotest.core.spec.Spec
import norm.executeCommand
import scalereal.core.models.domain.CheckInWithManagerRequest
import scalereal.core.models.domain.GoalParams
import scalereal.core.models.domain.Review
import scalereal.db.review.CheckInWithManagerRepositoryImpl
import util.StringSpecWithDataSource
import java.sql.Date

class CheckInWithManagerRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var checkInWithManagerRepositoryImpl: CheckInWithManagerRepositoryImpl

    init {
        "should add summary review" {
            checkInWithManagerRepositoryImpl.createCheckInWithManager(
                checkInWithManagerRequest =
                    CheckInWithManagerRequest(
                        organisationId = 1,
                        reviewTypeId = 3,
                        reviewDetailsId = 1,
                        reviewCycleId = 1,
                        reviewToId = 1,
                        reviewFromId = 2,
                        draft = false,
                        published = true,
                        firstManagerId = 2,
                        secondManagerId = null,
                        reviewData =
                            listOf(
                                Review(
                                    reviewId = 1,
                                    id = 1,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is a test review",
                                    rating = 5,
                                ),
                                Review(
                                    reviewId = 2,
                                    id = 2,
                                    kraId = null,
                                    kraName = null,
                                    kpiTitle = null,
                                    kpiDescription = null,
                                    review = "It is second test review",
                                    rating = 4,
                                ),
                            ),
                        goals =
                            listOf(
                                GoalParams(
                                    id = 1,
                                    goalId = 1,
                                    typeId = 1,
                                    assignedTo = 1,
                                    createdBy = 2,
                                    description = "1st goal",
                                    targetDate = Date.valueOf("2023-02-20"),
                                ),
                                GoalParams(
                                    id = 2,
                                    goalId = 2,
                                    typeId = 1,
                                    assignedTo = 1,
                                    createdBy = 2,
                                    description = "2nd goal",
                                    targetDate = Date.valueOf("2023-02-20"),
                                ),
                            ),
                        averageRating = 4.5.toBigDecimal(),
                    ),
            )
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                 INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size)
                 VALUES (1,1,'ScaleReal Technologies Pvt. Ltd.', true, 50);

                INSERT INTO review_cycle (
                  organisation_id,
                  id,
                  start_date,
                  end_date,
                  publish,
                  self_review_start_date,
                  self_review_end_date,
                  manager_review_start_date,
                  manager_review_end_date,
                  check_in_start_date,
                  check_in_end_date)
                VALUES
                      (1, 1, '12/12/2022', '02/02/2023', true,'12/13/2022','12/14/2022','01/01/2023','10/01/2023','01/20/2023','01/27/2023');

                INSERT INTO employees(id, emp_id, first_name, last_name, email_id, contact_no, status, organisation_id)
                VALUES
                      (1, 'SR0043', 'Syed Ubed', 'Ali', 'syed.ali@scalereal.com', 6262209099, true, 1),
                      (2, 'SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', 8265079426, true, 1);

                INSERT INTO kpi (organisation_id, id, kpi_id, title, description, status)
                VALUES
                      (1, 1, 1, '1st KPI Title', 'Its a detailed description of 1st kpi', true),
                      (1, 2, 2, '2nd KPI Title', 'Its a detailed description of 2nd kpi', true);

                INSERT INTO review_types (name)
                    SELECT review_type
                    FROM (VALUES
                    ('Self Review'),
                    ('Manager Review'),
                    ('Summary')) AS new_review_types(review_type)
                LEFT JOIN review_types ON new_review_types.review_type = review_types.name
                WHERE review_types.name IS NULL;

                INSERT INTO review_details
                VALUES
                      (1, 1, 1, 2, now(), true, false, 3);
                INSERT INTO reviews
                VALUES
                      (1, 1, 1, 'It is a test review in draft', 3),
                      (2, 1, 2, 'It is a 2nd test review in draft', 3);
                """.trimIndent(),
            )
        }
        checkInWithManagerRepositoryImpl = CheckInWithManagerRepositoryImpl(dataSource)
    }
}

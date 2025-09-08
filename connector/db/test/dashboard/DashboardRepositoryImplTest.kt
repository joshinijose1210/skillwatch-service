package dashboard

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.dashboard.DashboardService
import scalereal.core.models.domain.AppreciationData
import scalereal.core.models.domain.FeedbacksData
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.OverviewData
import scalereal.db.dashboard.DashboardRepositoryImpl
import util.StringSpecWithDataSource
import java.sql.Date
import java.sql.Timestamp
import java.time.OffsetDateTime

class DashboardRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var dashboardRepositoryImpl: DashboardRepositoryImpl
    private lateinit var dashboardService: DashboardService

    init {
        "should fetch review cycle and feedback overview by employee id" {
            val expectedResult =
                listOf(
                    OverviewData(
                        reviewCycleId = 1,
                        firstName = "Rushad",
                        startDate = Date.valueOf("2022-11-07"),
                        endDate = Date.valueOf("2023-06-25"),
                        selfReviewStartDate = Date.valueOf("2022-11-10"),
                        selfReviewEndDate = Date.valueOf("2022-11-18"),
                        selfReviewDraft = null,
                        selfReviewPublish = null,
                        positive = 2,
                        improvement = 1,
                        appreciation = 3,
                    ),
                )
            val feedbackOverview = dashboardRepositoryImpl.fetchFeedbackOverview(organisationId = 1, id = 1)
            feedbackOverview shouldBe expectedResult
        }

        "should fetch positive, improvement feedback and their count" {
            val expectedFeedback =
                listOf(
                    FeedbacksData(
                        isExternalFeedback = false,
                        feedbackType = "Improvement",
                        feedback = "negative feedback",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0006",
                        feedbackFromFirstName = "Yogesh",
                        feedbackFromLastName = "Jadhav",
                        feedbackFromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        submitDate = Timestamp.from(OffsetDateTime.parse("2023-01-26T18:39:50.791919+05:30").toInstant()),
                        isDraft = false,
                    ),
                    FeedbacksData(
                        isExternalFeedback = false,
                        feedbackType = "Positive",
                        feedback = "positive feedback",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0006",
                        feedbackFromFirstName = "Yogesh",
                        feedbackFromLastName = "Jadhav",
                        feedbackFromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        submitDate = Timestamp.from(OffsetDateTime.parse("2023-01-24T18:39:50.791919+05:30").toInstant()),
                        isDraft = false,
                    ),
                    FeedbacksData(
                        isExternalFeedback = false,
                        feedbackType = "Positive",
                        feedback = "positive feedback",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0006",
                        feedbackFromFirstName = "Yogesh",
                        feedbackFromLastName = "Jadhav",
                        feedbackFromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        submitDate = Timestamp.from(OffsetDateTime.parse("2022-12-26T18:39:50.791919+05:30").toInstant()),
                        isDraft = false,
                    ),
                )
            val organisationId = 1L
            val employeeId = listOf(-99)
            val reviewCycleId = listOf(1)
            val feedbackTypeId = listOf(1, 2)
            val count =
                dashboardRepositoryImpl.countEmployeeFeedback(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId,
                    feedbackTypeId = feedbackTypeId,
                )
            val actualFeedback =
                dashboardRepositoryImpl.fetchEmployeeFeedback(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId,
                    feedbackTypeId = feedbackTypeId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            count shouldBe actualFeedback.size
            actualFeedback shouldBe expectedFeedback
        }

        "should fetch appreciation feedback and its count" {
            val expectedFeedback =
                listOf(
                    AppreciationData(
                        isExternalFeedback = false,
                        appreciationToId = 1,
                        appreciationToEmployeeId = "SR0051",
                        appreciationToFirstName = "Rushad",
                        appreciationToLastName = "Shaikh",
                        appreciationToRoleName = "Employee",
                        appreciation = "Dummy appreciation feedback",
                        appreciationFromId = 2,
                        appreciationFromEmployeeId = "SR0006",
                        appreciationFromFirstName = "Yogesh",
                        appreciationFromLastName = "Jadhav",
                        appreciationFromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        submitDate = Timestamp.from(OffsetDateTime.parse("2023-03-24T18:39:50.791919+05:30").toInstant()),
                        isDraft = false,
                    ),
                    AppreciationData(
                        isExternalFeedback = false,
                        appreciationToId = 1,
                        appreciationToEmployeeId = "SR0051",
                        appreciationToFirstName = "Rushad",
                        appreciationToLastName = "Shaikh",
                        appreciationToRoleName = "Employee",
                        appreciation = "Appreciating for work",
                        appreciationFromId = 2,
                        appreciationFromEmployeeId = "SR0006",
                        appreciationFromFirstName = "Yogesh",
                        appreciationFromLastName = "Jadhav",
                        appreciationFromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        submitDate = Timestamp.from(OffsetDateTime.parse("2023-02-25T18:39:50.791919+05:30").toInstant()),
                        isDraft = false,
                    ),
                    AppreciationData(
                        isExternalFeedback = false,
                        appreciationToId = 1,
                        appreciationToEmployeeId = "SR0051",
                        appreciationToFirstName = "Rushad",
                        appreciationToLastName = "Shaikh",
                        appreciationToRoleName = "Employee",
                        appreciation = "Appreciation feedback",
                        appreciationFromId = 2,
                        appreciationFromEmployeeId = "SR0006",
                        appreciationFromFirstName = "Yogesh",
                        appreciationFromLastName = "Jadhav",
                        appreciationFromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        submitDate = Timestamp.from(OffsetDateTime.parse("2023-01-28T18:39:50.791919+05:30").toInstant()),
                        isDraft = false,
                    ),
                )
            val organisationId = 1L
            val employeeId = listOf(-99)
            val reviewCycleId = listOf(1)
            val feedbackTypeId = listOf(3)
            val count =
                dashboardRepositoryImpl.countEmployeeFeedback(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId,
                    feedbackTypeId = feedbackTypeId,
                )
            val actualFeedback =
                dashboardRepositoryImpl.fetchEmployeeFeedback(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId,
                    feedbackTypeId = feedbackTypeId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            count shouldBe actualFeedback.size
            actualFeedback shouldBe expectedFeedback
        }

        "should return count 0 if no feedback found for given review cycle" {
            val organisationId = 1L
            val employeeId = listOf(-99)
            val reviewCycleId = listOf(5)
            val feedbackTypeId = listOf(1, 2, 3)
            val count =
                dashboardRepositoryImpl.countEmployeeFeedback(
                    organisationId = organisationId,
                    id = employeeId,
                    reviewCycleId = reviewCycleId,
                    feedbackTypeId = feedbackTypeId,
                )
            count shouldBe 0
        }

        "should fetch goals and goals count for given review cycle id" {
            val expectedResult =
                listOf(
                    Goal(
                        id = 1,
                        goalId = "G1",
                        typeId = 1,
                        description = "First Action Item",
                        createdAt = Timestamp.valueOf("2023-02-21 03:04:33.863392"),
                        targetDate = Date.valueOf("2023-07-21"),
                        progressId = 1,
                        progressName = null,
                        createdBy = 2,
                        assignedTo = 1,
                    ),
                    Goal(
                        id = 2,
                        goalId = "G2",
                        typeId = 1,
                        description = "Second Action Item",
                        createdAt = Timestamp.valueOf("2023-02-21 03:04:33.863392"),
                        targetDate = Date.valueOf("2023-07-21"),
                        progressId = 1,
                        progressName = null,
                        createdBy = 2,
                        assignedTo = 1,
                    ),
                    Goal(
                        id = 3,
                        goalId = "G3",
                        typeId = 1,
                        description = "Third Action Item",
                        createdAt = Timestamp.valueOf("2023-02-21 03:04:33.863392"),
                        targetDate = Date.valueOf("2023-07-21"),
                        progressId = 1,
                        progressName = null,
                        createdBy = 2,
                        assignedTo = 1,
                    ),
                )
            val organisationId = 1L
            val reviewToId = 1L
            val reviewCycleId = 2
            val count =
                dashboardRepositoryImpl.countGoal(
                    organisationId = organisationId,
                    id = reviewToId,
                    reviewCycleId = reviewCycleId,
                )
            val goals =
                dashboardRepositoryImpl.fetchGoal(
                    organisationId = organisationId,
                    id = reviewToId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            count shouldBe goals.size
            goals shouldBe expectedResult
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size)
                VALUES (1,1,'ScaleReal Technologies Pvt. Ltd.', true, 50);

                INSERT INTO review_cycle(
                organisation_id,
                start_date,
                end_date,
                publish,
                self_review_start_date,
                self_review_end_date,
                manager_review_start_date,
                manager_review_end_date,
                check_in_start_date,
                check_in_end_date)
                VALUES (1, '2022-11-07','2023-06-25',true,'2022-11-10','2022-11-18','2022-11-12','2022-11-12','2023-02-20','2023-02-24'),
                       (1, '2023-07-01','2023-07-30',false,'2023-07-07','2023-07-18','2022-07-12','2023-07-18','2023-07-20','2023-05-24');


                INSERT INTO employees(id, emp_id, first_name, last_name, email_id, contact_no, status, onboarding_flow, organisation_id)
                VALUES (1, 'SR0051', 'Rushad', 'Shaikh', 'rushad.shaikh@scalereal.com', 8265079426, true, false, 1),
                       (2, 'SR0006', 'Yogesh', 'Jadhav', 'yogesh.jadhav@scalereal.com', 9876543210, true, false, 1);

                INSERT INTO roles(role_id, role_name, status, organisation_id)
                VALUES (1, 'Manager', true, 1),
                       (2, 'Employee', true, 1);

                INSERT INTO employees_role_mapping(emp_id, role_id)
                VALUES (1, 2),
                       (2, 1);

                INSERT INTO feedback_types (name)
                    SELECT feedback_type
                    FROM (VALUES
                    ('Positive'),
                    ('Improvement'),
                    ('Appreciation')) AS new_feedback_types(feedback_type)
                LEFT JOIN feedback_types ON new_feedback_types.feedback_type = feedback_types.name
                WHERE feedback_types.name IS NULL;

                INSERT INTO feedbacks(sr_no, created_at, feedback, feedback_to, feedback_from, feedback_type_id, request_id, is_draft, updated_at, feedback_from_external_id)
                VALUES(1, '2022-12-24 18:39:50.791919+05:30', 'positive feedback', 1, 2, 1, null, false, '2022-12-26 18:39:50.791919+05:30', null),
                      (2, '2023-01-01 18:39:50.791919+05:30', 'positive feedback', 1, 2, 1, null, false, '2023-01-24 18:39:50.791919+05:30', null),
                      (3, '2023-01-25 18:39:50.791919+05:30', 'negative feedback', 1, 2, 2, null, false, '2023-01-26 18:39:50.791919+05:30', null),
                      (4, '2023-01-27 18:39:50.791919+05:30', 'Appreciation feedback', 1, 2, 3, null, false, '2023-01-28 18:39:50.791919+05:30', null),
                      (5, '2023-02-24 18:39:50.791919+05:30', 'Appreciating for work', 1, 2, 3, null, false, '2023-02-25 18:39:50.791919+05:30', null),
                      (6, '2023-03-24 18:39:50.791919+05:30', 'Dummy appreciation feedback', 1, 2, 3, null, false, '2023-03-24 18:39:50.791919+05:30', null);

                INSERT INTO review_types (name)
                    SELECT review_type
                    FROM (VALUES
                    ('Self Review'),
                    ('Manager Review'),
                    ('Summary')) AS new_review_types(review_type)
                LEFT JOIN review_types ON new_review_types.review_type = review_types.name
                WHERE review_types.name IS NULL;

                INSERT INTO review_details(id, review_cycle_id, review_to, review_from, updated_at, draft, published, review_type_id)
                VALUES (1, 1, 1, 2, '2023-02-21 03:04:33.863392+00', true, true, 2), (2, 1, 1, 2, '2023-02-21 04:00:33.863392+00', true, true, 3);

                INSERT INTO goals (id, review_details_id, goal_id, organisation_id, assigned_to, created_by, description, type_id, progress_id, created_at, target_date)
                VALUES (1,1, 1, 1,1,2,'First Action Item', 1, 1, '2023-02-21 03:04:33.863392+00','2023-07-21'),
                       (2,1, 2, 1,1,2,'Second Action Item', 1, 1, '2023-02-21 03:04:33.863392+00', '2023-07-21'),
                       (3,1, 3, 1,1,2,'Third Action Item', 1, 1, '2023-02-21 03:04:33.863392+00', '2023-07-21');
                """.trimIndent(),
            )
        }
        dashboardRepositoryImpl = DashboardRepositoryImpl(dataSource)
        dashboardService = DashboardService(dashboardRepositoryImpl)
    }
}

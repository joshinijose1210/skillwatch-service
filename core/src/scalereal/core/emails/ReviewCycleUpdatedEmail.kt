package scalereal.core.emails

import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.organisations.OrganisationService
import java.time.format.DateTimeFormatter

@Singleton
class ReviewCycleUpdatedEmail(
    private val employeeService: EmployeeService,
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
) {
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun sendMail(reviewCycle: ReviewCycle) {
        val employeesEmail: List<String> = employeeService.getActiveEmployees(reviewCycle.organisationId).map { it.emailId }
        employeesEmail.map {
            emailSenderService.sendEmail(
                receiver = it,
                subject = "Important Update: Review Cycle Revision",
                htmlBody = reviewCycleUpdatedHTML(reviewCycle, organisationService.fetchName(reviewCycle.organisationId)),
                textBody = reviewCycleUpdatedTEXT(),
            )
        }
    }

    fun reviewCycleUpdatedHTML(
        reviewCycle: ReviewCycle,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>An important update regarding the current review cycle. " +
        "Our team has been working diligently to enhance our performance evaluation process, and as a result, " +
        "we have made some revisions to the review cycle.</P>" +
        "<P style = 'color:black;'><strong>Revised Timeline:</strong></P>" +
        "<ul style = 'padding-left:10px; color:black;'>" +
        "<li style = 'color:black;'><strong>Review Cycle Timeline</strong> - " +
        "${reviewCycle.startDate.toLocalDate().format(fullMonthDateFormatter)} to " +
        "${reviewCycle.endDate.toLocalDate().format(fullMonthDateFormatter)}</li>" +
        "<li style = 'color:black;'><strong>Self Review Timeline</strong> - " +
        "${reviewCycle.selfReviewStartDate.toLocalDate().format(fullMonthDateFormatter)} to " +
        "${reviewCycle.selfReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}</li>" +
        "<li style = 'color:black;'><strong>Manager Review Timeline</strong> - " +
        "${reviewCycle.managerReviewStartDate.toLocalDate().format(fullMonthDateFormatter)} to " +
        "${reviewCycle.managerReviewEndDate.toLocalDate().format(fullMonthDateFormatter)}</li>" +
        "<li style = 'color:black;'><strong>Check-in with Manager Timeline</strong> - " +
        "${reviewCycle.checkInWithManagerStartDate.toLocalDate().format(fullMonthDateFormatter)} to " +
        "${reviewCycle.checkInWithManagerEndDate.toLocalDate().format(fullMonthDateFormatter)}</li>" +
        "</ul>" +
        "<P style = 'color:black;'>If you have any questions or there's anything at all you're unclear about, " +
        "don't hesitate to reach out to the HR directly.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun reviewCycleUpdatedTEXT() = "Important Update: Review Cycle Revision"
}

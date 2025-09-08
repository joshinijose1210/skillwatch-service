package scalereal.core.emails

import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.organisations.OrganisationService
import java.time.format.DateTimeFormatter

@Singleton
class ReviewCycleStartedEmail(
    private val employeeService: EmployeeService,
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
) {
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun sendMail(reviewCycle: ReviewCycle) {
        val shortMonthDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val employeesEmail: List<String> = employeeService.getActiveEmployees(reviewCycle.organisationId).map { it.emailId }
        employeesEmail.map {
            emailSenderService.sendEmail(
                receiver = it,
                subject =
                    reviewCycleStartedSubject(
                        reviewCycle.startDate.toLocalDate().format(shortMonthDateFormatter),
                        reviewCycle.endDate.toLocalDate().format(shortMonthDateFormatter),
                    ),
                htmlBody = reviewCycleStartedHTML(reviewCycle, organisationService.fetchName(reviewCycle.organisationId)),
                textBody = reviewCycleStartedTEXT(),
            )
        }
    }

    fun reviewCycleStartedSubject(
        startDate: String,
        endDate: String,
    ) = "Performance Review Cycle has started ($startDate to $endDate)"

    fun reviewCycleStartedHTML(
        reviewCycle: ReviewCycle,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>We’re thrilled to announce that on " +
        "<strong>${reviewCycle.startDate.toLocalDate().format(fullMonthDateFormatter)}</strong> we'd be starting our new Review Cycle." +
        " This process is crucial in determining what progress we've made individually in contribution to the company's " +
        "objectives for the period in review. " +
        "<br>Here's all you need to know:</P>" +
        "<ul style = 'padding-left:10px; color:black;' >" +
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
        "<P style = 'color:black;'>" +
        "If you have any questions or there's anything at all you're unclear about, don't hesitate to reach out to your manager, " +
        "or the HR directly.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun reviewCycleStartedTEXT() = "Performance Review Cycle Started"
}

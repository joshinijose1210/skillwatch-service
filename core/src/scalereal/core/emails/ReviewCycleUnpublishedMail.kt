package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.organisations.OrganisationService
import java.time.format.DateTimeFormatter

@Singleton
class ReviewCycleUnpublishedMail(
    private val emailSenderService: EmailSenderService,
    private val employeeService: EmployeeService,
    private val organisationService: OrganisationService,
    @Inject private var appConfig: AppConfig,
) {
    private val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun sendMail(
        reviewCycle: ReviewCycle,
        organisationId: Long,
    ) {
        val shortMonthDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val employees = employeeService.getEmployeesWithReviewCyclePermission(organisationId)
        employees.map {
            emailSenderService.sendEmail(
                receiver = it.emailId,
                subject =
                    reviewCycleUnpublishedSubject(
                        reviewCycle.startDate.toLocalDate().format(shortMonthDateFormatter),
                        reviewCycle.endDate.toLocalDate().format(shortMonthDateFormatter),
                    ),
                htmlBody = reviewCycleUnpublishedHTML(reviewCycle, it.firstName, organisationService.fetchName(organisationId)),
                textBody = reviewCycleUnpublishedTEXT(),
            )
        }
    }

    private fun reviewCycleUnpublishedSubject(
        startDate: String,
        endDate: String,
    ) = "End of Previous Review Cycle ($startDate to $endDate), Create a New Review Cycle to Not Miss Out"

    private fun reviewCycleUnpublishedHTML(
        reviewCycle: ReviewCycle,
        employeeFirstName: String,
        organisationName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi $employeeFirstName,</P>" +
        "<P style = 'color:black;'>We want to inform you that the previous review cycle " +
        "(${reviewCycle.startDate.toLocalDate().format(fullMonthDateFormatter)} to " +
        "${reviewCycle.endDate.toLocalDate().format(fullMonthDateFormatter)}) has now come to a close." +
        " We appreciate your participation and valuable input throughout this process.</P>" +
        "<P style = 'color:black;'>However, we would like to remind you that it's essential to create a new review to ensure you don't" +
        " miss out on the upcoming review cycle. To start a new review cycle, please click on the below-mentioned link -</P>" +
        "<a href='$reviewCycleListingURL'>Add Review Cycle</a>" +
        "<P style = 'color:black;'>Please ensure that you create the new review cycle promptly and communicate the timeline and " +
        "expectations to the employees. " +
        "This will help us maintain a structured approach to performance management and maximize the benefits of the review process.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$organisationName</P>" +
        "</body>" +
        "</html>"

    private fun reviewCycleUnpublishedTEXT(): String = "Review Cycle Unpublished"

    private val instanceUrl = appConfig.getInstanceUrl()

    private val reviewCycleListingURL = "$instanceUrl/performance-review/review-cycles"
}

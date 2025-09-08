package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationService
import scalereal.core.reviewCycle.ReviewCycleService
import java.time.format.DateTimeFormatter

@Singleton
class SelfReviewSubmittedMail(
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    private val reviewCycleService: ReviewCycleService,
    private val employeeService: EmployeeService,
    @Inject private var appConfig: AppConfig,
) {
    fun sendMail(
        reviewTo: Long?,
        reviewCycleId: Long,
        organisationId: Long,
    ) {
        val shortMonthDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)
        val employeeEmail = employeeService.getEmployeeById(reviewTo).emailId

        emailSenderService.sendEmail(
            receiver = employeeEmail,
            subject =
                selfReviewSubmittedSubject(
                    reviewCycle.startDate.toLocalDate().format(shortMonthDateFormatter),
                    reviewCycle.endDate.toLocalDate().format(shortMonthDateFormatter),
                ),
            htmlBody = selfReviewSubmittedHTML(organisationService.fetchName(organisationId)),
            textBody = selfReviewSubmittedTEXT(),
        )
    }

    fun selfReviewSubmittedSubject(
        startDate: String,
        endDate: String,
    ) = "Self Review submitted for Review Cycle ($startDate to $endDate)"

    fun selfReviewSubmittedHTML(orgName: String) =
        "<!DOCTYPE html>" +
            "<html>" +
            "<body>" +
            "<P style = 'color:black;'>Hi there,</P>" +
            "<P style = 'color:black;'>Congratulations! Your self review for the current review cycle is submitted successfully.</P>" +
            "<P style = 'color:black;'>Please click on the below mentioned link to view your submitted self review -</P>" +
            "<a href='$selfReviewListingURL'>View Self Review</a>" +
            "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
            "<P style = 'color:black;'>Thank you & Regards,<br>" +
            "$orgName</P>" +
            "</body>" +
            "</html>"

    private fun selfReviewSubmittedTEXT() = "Self Review is submitted successfully"

    private val instanceUrl = appConfig.getInstanceUrl()

    private val selfReviewListingURL = "$instanceUrl/performance-review/self-review"
}

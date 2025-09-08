package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeService
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationService
import scalereal.core.reviewCycle.ReviewCycleService
import java.time.format.DateTimeFormatter

@Singleton
class ManagerReviewSubmittedMail(
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    private val reviewCycleService: ReviewCycleService,
    private val employeeService: EmployeeService,
    @Inject private var appConfig: AppConfig,
) {
    fun sendMailToEmployee(
        reviewFrom: Long?,
        reviewTo: Long?,
        reviewCycleId: Long,
        organisationId: Long,
    ) {
        var managerInfo = "Manager"
        var subjectText = "Manager"
        val shortMonthDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)
        val employeeData =
            employeeService
                .getEmployeeManagerList(
                    organisationId = organisationId,
                    id = listOf(reviewTo!!.toInt()),
                    firstManagerId = listOf(-99),
                    secondManagerId = listOf(-99),
                    page = 1,
                    limit = Int.MAX_VALUE,
                ).first()
        when (reviewFrom) {
            employeeData.firstManagerId -> {
                managerInfo = "Manager 1 " + "(${employeeData.firstManagerFirstName} ${employeeData.firstManagerLastName} " +
                    "- ${employeeData.firstManagerEmployeeId})"
                subjectText = "Manager 1"
            }
            employeeData.secondManagerId -> {
                managerInfo = "Manager 2 (${employeeData.secondManagerFirstName} ${employeeData.secondManagerLastName} " +
                    "- ${employeeData.secondManagerEmployeeId})"
                subjectText = "Manager 2"
            }
        }
        emailSenderService.sendEmail(
            receiver = employeeData.emailId,
            subject =
                managerReviewSubmittedToEmployeeSubject(
                    subjectText,
                    reviewCycle.startDate.toLocalDate().format(shortMonthDateFormatter),
                    reviewCycle.endDate.toLocalDate().format(shortMonthDateFormatter),
                ),
            htmlBody =
                managerReviewSubmittedToEmployeeHTML(
                    managerInfo,
                    organisationService.fetchName(organisationId = organisationId),
                ),
            textBody = managerReviewSubmittedTEXT(),
        )
    }

    fun sendMailToManager(
        reviewFrom: Long,
        reviewCycleId: Long,
        organisationId: Long,
    ) {
        val shortMonthDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val reviewCycle = reviewCycleService.fetchReviewCycle(reviewCycleId)
        val firstManagerEmployeeList =
            employeeService.getEmployeeManagerList(
                organisationId = organisationId,
                id = listOf(-99),
                firstManagerId = listOf(reviewFrom.toInt()),
                secondManagerId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
            )
        val secondManagerEmployeeList =
            employeeService.getEmployeeManagerList(
                organisationId = organisationId,
                id = listOf(-99),
                firstManagerId = listOf(-99),
                secondManagerId = listOf(reviewFrom.toInt()),
                page = 1,
                limit = Int.MAX_VALUE,
            )

        val firstManagerEmployees = firstManagerEmployeeList.map { it.getEmployeeNameWithEmployeeId() }
        val secondManagerEmployees = secondManagerEmployeeList.map { it.getEmployeeNameWithEmployeeId() }
        val allEmployees =
            (firstManagerEmployees + secondManagerEmployees).sortedWith(
                compareBy<String> { it.substringAfter(" ").substringBeforeLast(" ") }
                    .thenBy { it.substringAfterLast(" ") },
            )
        val receiverEmail = employeeService.getEmployeeById(reviewFrom).emailId
        emailSenderService.sendEmail(
            receiver = receiverEmail,
            subject =
                managerReviewSubmittedToManagerSubject(
                    reviewCycle.startDate.toLocalDate().format(shortMonthDateFormatter),
                    reviewCycle.endDate.toLocalDate().format(shortMonthDateFormatter),
                ),
            htmlBody =
                managerReviewSubmittedToManagerHTML(
                    allEmployees,
                    organisationService.fetchName(organisationId = organisationId),
                ),
            textBody = managerReviewSubmittedTEXT(),
        )
    }

    fun managerReviewSubmittedToEmployeeSubject(
        subjectText: String,
        startDate: String,
        endDate: String,
    ) = "$subjectText Review submitted for Review Cycle ($startDate to $endDate)"

    fun managerReviewSubmittedToManagerSubject(
        startDate: String,
        endDate: String,
    ) = "Manager Review submitted for Review Cycle ($startDate to $endDate)"

    fun managerReviewSubmittedToEmployeeHTML(
        managerInfo: String,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>Congratulations! Your $managerInfo Review for the current review cycle is submitted successfully.</P>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to view the submitted manager review -</P>" +
        "<a href='$myManagerReviewListingURL'>View Manager Review</a>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    fun managerReviewSubmittedToManagerHTML(
        employees: List<String>,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>Congratulations! The manager review for the current review cycle is submitted successfully " +
        "for the below mentioned employees.</P>" +
        "<P style = 'padding-left:10px; color:black;'>" +
        "<ul style = 'color:black;'>" +
        employees.map { "<li>$it</li>" }.joinToString(separator = "\n") +
        "</ul>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to view your submitted manager review -</P>" +
        "<a href='$reviewForTeamMembersURL'>View Manager Review</a>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun managerReviewSubmittedTEXT() = "Manager Review is submitted successfully"

    private val instanceUrl = appConfig.getInstanceUrl()

    private val myManagerReviewListingURL = "$instanceUrl/performance-review/manager-review"
    private val reviewForTeamMembersURL = """$instanceUrl/performance-review/team&apos;s-review"""
}

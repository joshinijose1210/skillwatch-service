package scalereal.core.emails

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationService
import java.sql.Date
import java.time.format.DateTimeFormatter

@Singleton
class SelfReviewReminderMail(
    private val emailSenderService: EmailSenderService,
    private val organisationService: OrganisationService,
    @Inject private var appConfig: AppConfig,
) {
    fun sendFiveDayReminderEmail(
        employeesEmailId: List<String>,
        endDate: Date,
        organisationId: Long,
    ) {
        val fullMonthDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        employeesEmailId.map {
            emailSenderService.sendEmail(
                receiver = it,
                subject = "Only 5 Days Left to Complete Your Self Review",
                htmlBody =
                    fiveDaysReminderHTML(
                        endDate.toLocalDate().format(fullMonthDateFormatter),
                        organisationService.fetchName(organisationId),
                    ),
                textBody = selfReviewReminderTEXT(),
            )
        }
    }

    fun sendLastDayReminderEmail(
        employeesEmailId: List<String>,
        organisationId: Long,
    ) {
        employeesEmailId.map {
            emailSenderService.sendEmail(
                receiver = it,
                subject = "Last day to Complete Your Self Review",
                htmlBody =
                    lastDayReminderHTML(
                        organisationService.fetchName(organisationId),
                    ),
                textBody = selfReviewReminderTEXT(),
            )
        }
    }

    fun fiveDaysReminderHTML(
        endDate: String,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi there,</P>" +
        "<P style = 'color:black;'>" +
        "Just sending you a friendly reminder to please complete your self review for your performance review by " +
        "<strong>$endDate</strong>.</P>" +
        "<P style = 'color:black;'>Performance reviews are a great opportunity for you to learn how much you have grown in your role," +
        " and to explore more growth opportunities with your manager.<br>" +
        "<P style = 'color:black;'>Please click on the below mentioned link to complete your self review -</P>" +
        "<a href='$reviewTimelineURL'>Start Self Review</a>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    fun lastDayReminderHTML(orgName: String) =
        "<!DOCTYPE html>" +
            "<html>" +
            "<body>" +
            "<P style = 'color:black;'>Hi there,</P>" +
            "<P style = 'color:black;'>" +
            "Just sending you a friendly reminder to please complete your self review for your performance review by " +
            "<strong>today</strong>.</P>" +
            "<P style = 'color:black;'>By now, you should have already submitted a self review for your performance review. " +
            "If you haven’t, please do so immediately.</P>" +
            "<P style = 'color:black;'>Please click on the below mentioned link to complete your self review -</P>" +
            "<a href='$reviewTimelineURL'>Start Self Review</a>" +
            "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
            "<P style = 'color:black;'>Thank you & Regards,<br>" +
            "$orgName</P>" +
            "</body>" +
            "</html>"

    private fun selfReviewReminderTEXT() = "Submit Self Review"

    private val reviewTimelineURL = appConfig.getReviewTimelineUrl()
}

package scalereal.core.emails

import jakarta.inject.Singleton
import scalereal.core.models.AppConfig
import scalereal.core.models.domain.PendingFeedbackRequestDetails
import java.sql.Date
import java.time.format.DateTimeFormatter

@Singleton
class FeedbackRequestReminderMail(
    private val emailSenderService: EmailSenderService,
    appConfig: AppConfig,
) {
    fun sendMail(feedbackRequestData: PendingFeedbackRequestDetails) {
        val receiver = feedbackRequestData.emailToUse()
        receiver?.let {
            val requestedByName = feedbackRequestData.requestedByDetails()
            val shortDate = formatDate(feedbackRequestData.date, "d MMM yyyy")
            val longDate = formatDate(feedbackRequestData.date, "d MMMM yyyy")

            emailSenderService.sendEmail(
                receiver = it,
                subject = feedbackRequestReminderSubject(requestedByName, shortDate),
                htmlBody =
                    feedbackRequestReminderHTML(
                        feedbackFromName = feedbackRequestData.feedbackFromFirstName,
                        requestedByName = requestedByName,
                        orgName = feedbackRequestData.organisationName,
                        date = longDate,
                    ),
                textBody = feedbackRequestReminderTEXT(),
            )
        }
    }

    private fun PendingFeedbackRequestDetails.emailToUse(): String? =
        if (isExternalRequest) externalFeedbackFromEmailId else feedbackFromEmailId

    private fun PendingFeedbackRequestDetails.requestedByDetails(): String =
        "$requestedByFirstName $requestedByLastName ($requestedByEmpId)"

    private fun formatDate(
        date: Date,
        pattern: String,
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern(pattern)
        return date.toLocalDate().format(dateFormatter)
    }

    private fun feedbackRequestReminderSubject(
        requestedByName: String,
        date: String,
    ) = "Gentle Reminder: Pending Feedback Request from $requestedByName on $date"

    private fun feedbackRequestReminderHTML(
        feedbackFromName: String?,
        requestedByName: String,
        orgName: String,
        date: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Dear ${feedbackFromName ?: ""},</P>" +
        "<P style = 'color:black;'>" +
        "I hope this email finds you well. I wanted to gently remind you about the pending feedback request from $requestedByName " +
        "dated $date. " +
        "Your input is highly valuable to us, and $requestedByName is looking forward to receiving your feedback.</P>" +
        "<P style = 'color:black;'>" +
        "If you haven't had a chance to submit your feedback yet, please take a moment to share your thoughts. " +
        "Your insights play a crucial role in helping us enhance our services.</P>" +
        "<P style = 'color:black;'>Please click on the below-mentioned link to submit your feedback: " +
        "<a href='$requestFeedbackURL'>Submit Feedback</a></P>" +
        "<P style = 'color:black;'>If you need any assistance or clarification, please feel free to reach out to the HR.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun feedbackRequestReminderTEXT() = "Feedback Request Reminder"

    private val requestFeedbackURL = appConfig.getRequestFeedbackUrl()
}

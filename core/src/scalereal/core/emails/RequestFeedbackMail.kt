package scalereal.core.emails

import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.models.AppConfig
import scalereal.core.organisations.OrganisationRepository
import java.util.Base64

@Singleton
class RequestFeedbackMail(
    private val emailSenderService: EmailSenderService,
    private val employeeRepository: EmployeeRepository,
    private val organisationRepository: OrganisationRepository,
    private val appConfig: AppConfig,
) {
    fun sendRequestReceivedMail(
        requestedById: Long,
        feedbackFromId: List<Long>,
        feedbackToId: List<Long>,
        request: String,
    ) {
        val requestedByDetails = employeeRepository.getEmployeeById(requestedById)
        val requestedByData = "${requestedByDetails.firstName} ${requestedByDetails.lastName} (${requestedByDetails.employeeId})"
        val organisationDetails = organisationRepository.getOrganisationDetails(requestedByDetails.organisationId)

        feedbackFromId
            .flatMap { feedbackFrom ->
                val feedbackFromDetails = employeeRepository.getEmployeeById(feedbackFrom)

                feedbackToId.map { feedbackTo ->
                    val feedbackToDetails = employeeRepository.getEmployeeById(feedbackTo)

                    val receiver = feedbackFromDetails.emailId

                    val subject: String
                    val htmlBody: String
                    val textBody: String

                    if (feedbackTo == requestedById) {
                        subject = "Feedback Request from $requestedByData about their performance"
                        htmlBody =
                            requestReceivedMailHTML(
                                feedbackToDetail = "their",
                                feedbackRequest = request,
                                feedbackFromName = feedbackFromDetails.firstName,
                                requestedByData = requestedByData,
                                orgName = organisationDetails.name,
                            )
                        textBody = requestReceivedMailTEXT()
                    } else {
                        val feedbackToFullName = "${feedbackToDetails.firstName} ${feedbackToDetails.lastName}"
                        val feedbackToData = "$feedbackToFullName's (${feedbackToDetails.employeeId})"
                        subject = "Feedback Request from $requestedByData about $feedbackToFullName (${feedbackToDetails.employeeId})"
                        htmlBody =
                            requestReceivedMailHTML(
                                feedbackToDetail = feedbackToData,
                                feedbackRequest = request,
                                feedbackFromName = feedbackFromDetails.firstName,
                                requestedByData = requestedByData,
                                orgName = organisationDetails.name,
                            )
                        textBody = requestReceivedMailTEXT()
                    }

                    Triple(receiver, subject, Pair(htmlBody, textBody))
                }
            }.forEach { (receiver, subject, bodyPair) ->
                emailSenderService.sendEmail(
                    receiver = receiver,
                    subject = subject,
                    htmlBody = bodyPair.first,
                    textBody = bodyPair.second,
                )
            }
    }

    fun sendRequestedFeedbackReceivedMail(
        requestedById: Long,
        feedbackFromId: Long,
    ) {
        val requestedByDetails = employeeRepository.getEmployeeById(requestedById)
        val feedbackFromDetails = employeeRepository.getEmployeeById(feedbackFromId)
        val feedbackFromData = "${feedbackFromDetails.firstName} ${feedbackFromDetails.lastName} (${feedbackFromDetails.employeeId})"
        val organisationDetails = organisationRepository.getOrganisationDetails(requestedByDetails.organisationId)
        emailSenderService.sendEmail(
            receiver = requestedByDetails.emailId,
            subject = "Feedback received on your request by $feedbackFromData",
            htmlBody = requestedFeedbackReceivedMailHTML(feedbackFromData, requestedByDetails.firstName, organisationDetails.name),
            textBody = requestReceivedMailTEXT(),
        )
    }

    fun sendExternalFeedbackRequestMail(
        requestedByName: String,
        feedbackToName: String,
        feedbackFromEmail: String,
        request: String,
        requestId: Long,
        organisationName: String,
        isSelfRequest: Boolean,
    ) {
        val encodedRequestId = Base64.getEncoder().encodeToString(requestId.toString().toByteArray())
        emailSenderService.sendEmail(
            receiver = feedbackFromEmail,
            subject = "Feedback Request for $feedbackToName from $organisationName",
            htmlBody =
                externalFeedbackRequestHTML(
                    requestId = encodedRequestId,
                    feedbackToName = feedbackToName,
                    requestedByName = requestedByName,
                    feedbackRequest = request,
                    orgName = organisationName,
                    isSelfRequest = isSelfRequest,
                ),
            textBody = "Feedback Form",
        )
    }

    private fun externalFeedbackRequestHTML(
        requestId: String,
        feedbackToName: String,
        requestedByName: String,
        feedbackRequest: String,
        orgName: String,
        isSelfRequest: Boolean,
    ): String {
        val introLine =
            if (isSelfRequest) {
                "$requestedByName has requested your valuable feedback about their performance as part of our review process."
            } else {
                "$requestedByName has requested your valuable feedback about $feedbackToName as part of our review process."
            }
        return "<!DOCTYPE html>" +
            "<html>" +
            "<body>" +
            "<P style = 'color:black;'>Hi,</P>" +
            "<P style='color:black;'>We hope you're doing well.</P>" +
            "<P style='color:black;'>$introLine " +
            "Your perspective on the below context will help us better understand their strengths," +
            " contributions, and areas they can grow in.</P>" +
            "<P style='color:black;'><strong>Context:</strong><br>$feedbackRequest</P>" +
            "<P style='color:black;'>Here’s a quick form to share your thoughts and should take less than 2–3 minutes.</P>" +
            "<a href='$feedbackFormURL?" +
            "id=${emailSenderService.insertLinkDetailsAndGetUniqueId("External feedback form")}&requestId=$requestId'>" +
            "Feedback Form</a>" +
            "<P style='color:black;'>Feel free to share anything — from things they’re doing well to any suggestion for " +
            "improvements or even a quick appreciation note!</P>" +
            "<P style='color:black;'>Thanks so much for taking the time. Really appreciate it.</P>" +
            "<P style='color:black;'>Thank you & Regards,<br>" +
            "$requestedByName<br>" +
            "$orgName</P>" +
            "</body>" +
            "</html>"
    }

    private fun requestedFeedbackReceivedMailHTML(
        feedbackFrom: String,
        requestedByName: String,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>Hi $requestedByName,</P>" +
        "<P style = 'color:black;'>We want to inform you that $feedbackFrom has provided feedback for the feedback request raised.. " +
        "Kindly click on the below mentioned link to view the feedback received. </P>" +
        "<a href='$requestFeedbackURL'>View Feedback Received</a>" +
        "<P style = 'color:black;'>" +
        "If you have any questions or there's anything at all you're unclear about, " +
        "don't hesitate to reach out to the HR directly.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun requestReceivedMailHTML(
        feedbackToDetail: String,
        feedbackRequest: String,
        feedbackFromName: String,
        requestedByData: String,
        orgName: String,
    ) = "<!DOCTYPE html>" +
        "<html>" +
        "<body>" +
        "<P style = 'color:black;'>We hope you're doing well.</P>" +
        "<P style = 'color:black;'>$requestedByData has requested your valuable feedback " +
        "about $feedbackToDetail performance as part of our review process." +
        "Your perspective on the below context will help us better understand their strengths, " +
        "contributions, and areas they can grow in.</P>" +
        "<P style = 'color:black;'><strong>Context:</strong><br>$feedbackRequest</P>" +
        "<P style = 'color:black;'>Here’s a quick form to share your thoughts and should take less than 2–3 minutes.</P>" +
        "<a href='$requestFeedbackURL'>Feedback Form Link</a>" +
        "<P style = 'color:black;'>Feel free to share anything — from things they’re doing well to any suggestion for " +
        "improvements or even a quick appreciation note!</P>" +
        "<P style = 'color:black;'>Thanks so much for taking the time. Really appreciate it.</P>" +
        "<P style = 'color:black;'>Thank you & Regards,<br>" +
        "$feedbackFromName<br>" +
        "$orgName</P>" +
        "</body>" +
        "</html>"

    private fun requestReceivedMailTEXT() = "Feedback request received mail"

    private val instanceUrl = appConfig.getInstanceUrl()

    private val requestFeedbackURL = appConfig.getRequestFeedbackUrl()

    private val feedbackFormURL = "$instanceUrl/submit-feedback"
}

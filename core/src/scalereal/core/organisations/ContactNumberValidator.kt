package scalereal.core.organisations

import com.google.i18n.phonenumbers.PhoneNumberUtil
import jakarta.inject.Singleton
import scalereal.core.models.domain.ValidContactNumber

@Singleton
class ContactNumberValidator {
    fun isValidContactNumber(contactNo: String): ValidContactNumber {
        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        try {
            val contactNumberWithCountryCode = if (contactNo.startsWith("+")) contactNo else "+$contactNo"
            val parsedPhoneNumber = phoneNumberUtil.parse(contactNumberWithCountryCode, null)
            return ValidContactNumber(
                isValid = phoneNumberUtil.isValidNumber(parsedPhoneNumber),
                formattedContact = phoneNumberUtil.format(parsedPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164),
            )
        } catch (e: Exception) {
            return ValidContactNumber(
                isValid = false,
                formattedContact = null,
            )
        }
    }
}

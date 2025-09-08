package scalereal.core.models.domain

import java.time.LocalDate

data class ValidDate(
    val isValid: Boolean,
    val formattedDate: LocalDate?,
)

package org.example.data.mapper.sync

import org.example.data.exception.NullRequiredFieldException

internal fun <T : Any> T?.requiredField(
    message: String
): T {
    return this ?: throw NullRequiredFieldException(message)
}

package org.example.data.validation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.PackageResponseDto

class PackageRemoteValidator {

    fun validate(
        dto: PackageResponseDto
    ) {

        if (dto.weight == null) {

            throw NullRequiredFieldException(
                "Package '${dto.id}' has null weight."
            )
        }
    }
}

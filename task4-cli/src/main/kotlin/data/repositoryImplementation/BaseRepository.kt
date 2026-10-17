package org.example.data.repositoryImplementation

import org.example.data.exception.NullRequiredFieldException

abstract class BaseRepository {

    protected val warnings =
        mutableListOf<String>()

    protected fun <T> mapSafely(
        id: String,
        block: () -> T
    ): T? {

        return runCatching {

            block()

        }.getOrElse { exception ->

            if (exception is NullRequiredFieldException) {

                warnings.add(
                    "$id: ${exception.message}"
                )

                null

            } else {

                throw exception
            }
        }
    }
}

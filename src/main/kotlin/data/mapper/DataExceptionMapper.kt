package org.example.data.mapper

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import org.example.data.exception.DataException
import org.example.data.exception.EmptyFileDataException
import org.example.data.exception.FileNotFoundDataException
import org.example.data.exception.InvalidColumnCountException
import org.example.data.exception.MissingRequiredFieldException
import org.example.data.exception.NullRequiredFieldException
import org.example.domain.model.exception.DataAccessException
import org.example.domain.model.exception.DomainException
import org.example.domain.model.exception.InvalidDataException
import org.example.domain.model.exception.NetworkException
import org.example.domain.model.exception.UnknownException

class DataExceptionMapper {

    fun map(exception: Throwable): DomainException {
        if (exception is CancellationException) {
            throw exception
        }

        return when (exception) {
            is DomainException -> exception

            is FileNotFoundDataException -> {
                DataAccessException(cause = exception)
            }

            is EmptyFileDataException,
            is InvalidColumnCountException,
            is MissingRequiredFieldException,
            is NullRequiredFieldException -> {
                InvalidDataException(cause = exception)
            }

            is DataException -> {
                DataAccessException(cause = exception)
            }

            is HttpRequestTimeoutException,
            is ConnectTimeoutException,
            is SocketTimeoutException,
            is IOException -> {
                NetworkException(cause = exception)
            }

            is ClientRequestException -> {
                mapClientRequestException(exception)
            }

            is ServerResponseException -> {
                NetworkException(cause = exception)
            }

            is SerializationException,
            is NumberFormatException -> {
                InvalidDataException(cause = exception)
            }

            else -> {
                UnknownException(cause = exception)
            }
        }
    }

    private fun mapClientRequestException(
        exception: ClientRequestException
    ): DomainException {
        return when (exception.response.status) {
            HttpStatusCode.RequestTimeout,
            HttpStatusCode.TooManyRequests -> {
                NetworkException(cause = exception)
            }

            HttpStatusCode.BadRequest,
            HttpStatusCode.UnprocessableEntity -> {
                InvalidDataException(cause = exception)
            }

            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden,
            HttpStatusCode.NotFound,
            HttpStatusCode.Conflict -> {
                DataAccessException(cause = exception)
            }

            else -> {
                DataAccessException(cause = exception)
            }
        }
    }
}

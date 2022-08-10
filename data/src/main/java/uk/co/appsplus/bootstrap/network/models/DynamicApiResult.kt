package uk.co.appsplus.bootstrap.network.models

import okhttp3.ResponseBody
import java.io.IOException

sealed class DynamicApiResult<out R> {
    data class Success<R>(val result: R) : DynamicApiResult<R>()
    data class Failure(val error: DynamicApiError) : DynamicApiResult<Nothing>()

    companion object {
        inline fun <reified S> fromError(
            error: ResponseBody?
        ): DynamicApiResult<S> {
            return Failure(
                DynamicApiError.from(error)
            )
        }

        fun <S> fromException(
            exception: Throwable
        ): DynamicApiResult<S> {
            return Failure(
                when (exception) {
                    is IOException -> DynamicApiError.NotConnected
                    else -> DynamicApiError.Unknown
                }
            )
        }
    }
}

sealed class DynamicApiError {
    object NotConnected : DynamicApiError()
    object Unknown : DynamicApiError()
    data class Message(val message: String) : DynamicApiError()
    data class Validation(
        val errors: DynamicServerValidation
    ) : DynamicApiError()

    companion object {
        fun from(
            response: ResponseBody?
        ): DynamicApiError {
            val responseString = response?.string()
            return responseString
                ?.dynamicValidationErrors()
                ?.let { Validation(it) }
                ?: responseString
                    ?.serverMessage()
                    ?.let { Message(it.message) }
                ?: Unknown
        }
    }
}

fun DynamicApiError.Validation.validationErrors(field: String) : List<String>? {
    return errors[field]
}

fun DynamicApiResult.Failure.validationErrors(field: String) : List<String>? {
    return (error as? DynamicApiError.Validation)?.validationErrors(field)
}
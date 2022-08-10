package uk.co.appsplus.bootstrap.network.models

import okhttp3.ResponseBody
import java.io.IOException

sealed class ApiResult<out R, out F : ValidationField> {
    data class Success<R>(val result: R) : ApiResult<R, Nothing>()
    data class Failure<F : ValidationField>(val error: ApiError<F>) : ApiResult<Nothing, F>()

    companion object {
        inline fun <reified S, reified Field> fromError(
            error: ResponseBody?
        ): ApiResult<S, Field> where Field : Enum<Field>, Field : ValidationField  {
            return Failure(
                ApiError.from(error)
            )
        }

        inline fun <reified S, reified Field> fromError(
            error: ResponseBody?,
            clazz: Class<Field>,
        ): ApiResult<S, Field> where Field : Enum<Field>, Field : ValidationField  {
            return Failure(
                ApiError.from(error)
            )
        }

        fun <S, Field> fromException(
            exception: Throwable
        ): ApiResult<S, Field> where Field : Enum<Field>, Field : ValidationField  {
            return Failure(
                when (exception) {
                    is IOException -> ApiError.NotConnected
                    else -> ApiError.Unknown
                }
            )
        }

        fun <S, Field> fromException(
            exception: Throwable,
            clazz: Class<Field>,
        ): ApiResult<S, Field> where Field : Enum<Field>, Field : ValidationField  {
            return Failure(
                when (exception) {
                    is IOException -> ApiError.NotConnected
                    else -> ApiError.Unknown
                }
            )
        }
    }
}

sealed class ApiError<out F : ValidationField> {
    object NotConnected : ApiError<Nothing>()
    object Unknown : ApiError<Nothing>()
    data class Message(val message: String) : ApiError<Nothing>()
    data class Validation<F : ValidationField>(
        val errors: ServerValidation<F>
    ) : ApiError<F>()

    companion object {
        inline fun <reified V> from(
            response: ResponseBody?
        ): ApiError<V> where V : Enum<V>, V : ValidationField {
            val responseString = response?.string()
            return responseString
                ?.validationErrors<V>()
                ?.let { Validation(it) }
                ?: responseString
                    ?.serverMessage()
                    ?.let { Message(it.message) }
                ?: Unknown
        }
    }
}

fun <F> ApiError.Validation<F>.validationErrors(field: F) : List<String>? where F : ValidationField {
    return errors[field]
}

fun <F> ApiResult.Failure<F>.validationErrors(field: F) : List<String>? where F : ValidationField {
    return (error as? ApiError.Validation<F>)?.validationErrors(field)
}

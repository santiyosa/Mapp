package com.maintenance.app.utils

/**
 * A generic wrapper class that represents the result of an operation.
 * It can be either a Success with data or an Error with exception/message.
 */
sealed class Result<out T> {
    
    data class Success<out T>(val data: T) : Result<T>()
    
    data class Error(
        val exception: Throwable? = null,
        val message: String = exception?.message ?: "Unknown error"
    ) : Result<Nothing>()

    data class Loading(val message: String = "Loading...") : Result<Nothing>()

    /**
     * Returns true if this is a Success result.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this is an Error result.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns true if this is a Loading result.
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Returns the data if Success, null otherwise.
     */
    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }

    /**
     * Returns the data if Success, or the default value if Error/Loading.
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T {
        return when (this) {
            is Success -> data
            else -> defaultValue
        }
    }

    /**
     * Returns the exception if Error, null otherwise.
     */
    fun exceptionOrNull(): Throwable? {
        return when (this) {
            is Error -> exception
            else -> null
        }
    }

    /**
     * Transforms the data if Success, otherwise returns the same Error/Loading.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }

    /**
     * Flat maps the result if Success, otherwise returns the same Error/Loading.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> {
        return when (this) {
            is Success -> transform(data)
            is Error -> this
            is Loading -> this
        }
    }

    /**
     * Performs the given action on the data if Success.
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    /**
     * Performs the given action on the error if Error.
     */
    inline fun onError(action: (Throwable?, String) -> Unit): Result<T> {
        if (this is Error) {
            action(exception, message)
        }
        return this
    }

    /**
     * Performs the given action if Loading.
     */
    inline fun onLoading(action: (String) -> Unit): Result<T> {
        if (this is Loading) {
            action(message)
        }
        return this
    }

    companion object {
        /**
         * Creates a Success result with the given data.
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Creates an Error result with the given exception.
         */
        fun error(exception: Throwable): Result<Nothing> = Error(exception)

        /**
         * Creates an Error result with the given message.
         */
        fun error(message: String): Result<Nothing> = Error(message = message)

        /**
         * Creates a Loading result with the given message.
         */
        fun loading(message: String = "Loading..."): Result<Nothing> = Loading(message)

        /**
         * Wraps a suspend function call in a try-catch and returns Result.
         */
        suspend inline fun <T> safeCall(crossinline call: suspend () -> T): Result<T> {
            return try {
                Success(call())
            } catch (e: Exception) {
                Error(e)
            }
        }

        /**
         * Wraps a regular function call in a try-catch and returns Result.
         */
        inline fun <T> safeSyncCall(crossinline call: () -> T): Result<T> {
            return try {
                Success(call())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
}
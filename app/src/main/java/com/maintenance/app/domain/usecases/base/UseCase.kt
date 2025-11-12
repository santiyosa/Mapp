package com.maintenance.app.domain.usecases.base

import com.maintenance.app.utils.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class for Use Cases that execute operations and return Result.
 */
abstract class UseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Executes the use case operation.
     */
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters).let {
                    Result.success(it)
                }
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    /**
     * Override this to set the code to be executed.
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): R
}

/**
 * Base class for Use Cases that don't require parameters.
 */
abstract class UseCaseNoParams<R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Executes the use case operation.
     */
    suspend operator fun invoke(): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute().let {
                    Result.success(it)
                }
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    /**
     * Override this to set the code to be executed.
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(): R
}

/**
 * Base class for Use Cases that return Flow (no Result wrapping needed).
 */
abstract class FlowUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Executes the use case operation.
     */
    suspend operator fun invoke(parameters: P): R {
        return withContext(coroutineDispatcher) {
            execute(parameters)
        }
    }

    /**
     * Override this to set the code to be executed.
     */
    protected abstract suspend fun execute(parameters: P): R
}

/**
 * Base class for Flow Use Cases that don't require parameters.
 */
abstract class FlowUseCaseNoParams<R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Executes the use case operation.
     */
    suspend operator fun invoke(): R {
        return withContext(coroutineDispatcher) {
            execute()
        }
    }

    /**
     * Override this to set the code to be executed.
     */
    protected abstract suspend fun execute(): R
}
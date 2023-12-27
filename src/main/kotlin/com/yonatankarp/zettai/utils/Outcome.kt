package com.yonatankarp.zettai.utils

import com.yonatankarp.zettai.utils.Outcome.Failure
import com.yonatankarp.zettai.utils.Outcome.Success

sealed class Outcome<out E : OutcomeError, out T> {
    data class Success<T> internal constructor(val value: T) : Outcome<Nothing, T>()

    data class Failure<E : OutcomeError> internal constructor(val error: E) : Outcome<E, Nothing>()

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> f(value).asSuccess()
            is Failure -> this
        }

    fun <F : OutcomeError> transformFailure(f: (E) -> F): Outcome<F, T> =
        when (this) {
            is Success -> this
            is Failure -> f(error).asFailure()
        }

    fun <T> tryAndCatch(block: () -> T): Outcome<ThrowableError, T> =
        try {
            block().asSuccess()
        } catch (t: Throwable) {
            ThrowableError(t).asFailure()
        }

    companion object {
        fun <T, U, E : OutcomeError> lift(f: (T) -> U): (Outcome<E, T>) -> Outcome<E, U> = { o -> o.transform { f(it) } }

        fun <T, E : OutcomeError> Outcome<E, T>.recover(recoverError: (E) -> T): T =
            when (this) {
                is Success -> value
                is Failure -> recoverError(error)
            }
    }
}

interface OutcomeError {
    val msg: String
}

inline fun <T, E : OutcomeError> Outcome<E, T>.onFailure(exitBlock: (E) -> Nothing): T =
    when (this) {
        is Success<T> -> value
        is Failure<E> -> exitBlock(error)
    }

fun <E : OutcomeError> E.asFailure(): Outcome<E, Nothing> = Failure(this)

fun <T> T.asSuccess(): Outcome<Nothing, T> = Success(this)

fun <T : Any, E : OutcomeError> T?.failIfNull(error: E): Outcome<E, T> = this?.asSuccess() ?: error.asFailure()

data class ThrowableError(val t: Throwable) : OutcomeError {
    override val msg: String = t.message.orEmpty()
}

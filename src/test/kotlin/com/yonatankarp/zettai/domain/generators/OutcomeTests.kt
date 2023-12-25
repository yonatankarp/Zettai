package com.yonatankarp.zettai.domain.generators

import com.yonatankarp.zettai.utils.Outcome
import com.yonatankarp.zettai.utils.OutcomeError
import com.yonatankarp.zettai.utils.onFailure
import org.junit.jupiter.api.fail

fun <E : OutcomeError, T> Outcome<E, T>.expectSuccess(): T = onFailure { error -> fail { "$this expected success but was $error" } }

fun <E : OutcomeError, T> Outcome<E, T>.expectFailure(): E =
    onFailure { error -> return error }
        .let { fail { "Expected failure but was $it" } }

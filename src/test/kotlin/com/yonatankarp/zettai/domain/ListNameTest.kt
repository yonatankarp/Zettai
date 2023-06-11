package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.domain.generators.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ListNameTest {

    companion object {
        private const val validCharset = uppercase + lowercase + digits + "-"
        private const val invalidCharset = " !@#\$%^&*()_+={}[]|:;'<>,./?\u2202\u2203\u2204\u2205”"
    }

    @Test
    fun `Valid names are alphanum+hiphen between 3 and 40 chars length`() {
        stringsGenerator(validCharset, 3, 40)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it))
                    .isEqualTo(ListName.fromTrusted(it))
            }
    }

    @Test
    fun `Name cannot be empty`() {
        expectThat(ListName.fromUntrusted("")).isEqualTo(null)
    }

    @Test
    fun `Names longer then 40 chars are not valid`() {
        stringsGenerator(validCharset, 41, 200)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isEqualTo(null)
            }
    }

    @Test
    fun `Invalid chars are not allowed in the name`() {
        stringsGenerator(validCharset, 1, 30)
            .map { substituteRandomChar(invalidCharset, it) }
            .take(1000)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isEqualTo(null)
            }
    }
}

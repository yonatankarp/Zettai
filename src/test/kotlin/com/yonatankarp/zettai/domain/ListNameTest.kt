package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.domain.generators.DIGITS
import com.yonatankarp.zettai.domain.generators.LOWERCASE
import com.yonatankarp.zettai.domain.generators.UPPERCASE
import com.yonatankarp.zettai.domain.generators.stringsGenerator
import com.yonatankarp.zettai.domain.generators.substituteRandomChar
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ListNameTest {
    companion object {
        private const val VALID_CHARSET = UPPERCASE + LOWERCASE + DIGITS + "-"
        private const val INVALID_CHARSET = " !@#\$%^&*()_+={}[]|:;'<>,./?\u2202\u2203\u2204\u2205”"
    }

    @Test
    fun `Valid names are alphanum+hiphen between 3 and 40 chars length`() {
        stringsGenerator(VALID_CHARSET, 3, 40)
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
        stringsGenerator(VALID_CHARSET, 41, 200)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isEqualTo(null)
            }
    }

    @Test
    fun `Invalid chars are not allowed in the name`() {
        stringsGenerator(VALID_CHARSET, 1, 30)
            .map { substituteRandomChar(INVALID_CHARSET, it) }
            .take(1000)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isEqualTo(null)
            }
    }
}

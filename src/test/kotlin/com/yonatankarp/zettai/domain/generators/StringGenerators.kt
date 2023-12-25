package com.yonatankarp.zettai.domain.generators

import kotlin.random.Random

const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
const val DIGITS = "0123456789"

fun stringsGenerator(
    charSet: String,
    minLen: Int,
    maxLen: Int,
): Sequence<String> =
    generateSequence {
        randomString(charSet, minLen, maxLen)
    }

fun randomString(
    charSet: String = UPPERCASE + LOWERCASE + DIGITS,
    minLen: Int = 5,
    maxLen: Int = 20,
) = StringBuilder().run {
    val len = if (maxLen > minLen) Random.nextInt(maxLen - minLen) + minLen else minLen
    repeat(len) {
        append(charSet.random())
    }
    toString()
}

fun substituteRandomChar(
    fromCharset: String,
    intoString: String,
): String =
    intoString
        .toCharArray()
        .apply { set(Random.nextInt(intoString.length), fromCharset.random()) }
        .joinToString(separator = "")

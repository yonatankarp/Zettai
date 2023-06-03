package com.yonatankarp.zettai.utils

typealias FUN<A, B> = (A) -> B

/**
 * Composes two functions together.
 */
infix fun <A, B, C> FUN<A, B>.andThen(f: FUN<B, C>): FUN<A, C> = { a: A -> f(this(a)) }
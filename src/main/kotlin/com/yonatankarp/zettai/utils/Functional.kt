package com.yonatankarp.zettai.utils

typealias FUN<A, B> = (A) -> B

/**
 * Composes two functions together.
 */
infix fun <A, B, C> FUN<A, B>.andThen(f: FUN<B, C>): FUN<A, C> = { a: A -> f(this(a)) }

/**
 * Applies the given function to the sequence if it is not null or return null if it does.
 */
fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
    if (isNullOrEmpty()) null else f(this)


/**
 * A debug utility function to print the value of a variable.
 */
fun <T> T.printIt(prefix: String = ">"): T = also{ println("$prefix $this") }

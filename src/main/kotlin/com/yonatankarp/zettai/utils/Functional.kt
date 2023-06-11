package com.yonatankarp.zettai.utils

typealias FUN<A, B> = (A) -> B

/**
 * Composes two functions together.
 */
infix fun <A, B, C> FUN<A, B>.andThen(f: FUN<B, C>): FUN<A, C> = { a: A -> f(this(a)) }

/**
 * Composes two functions together if the value is not null, otherwise will return null without
 * continuing the calculation.
 */
infix fun <A : Any, B : Any, C : Any> FUN<A, B?>.andUnlessNull(other: FUN<B, C?>): FUN<A, C?> =
    { a: A -> this(a)?.let { other(it) } }

/**
 * Applies the given function to the sequence if it is not null or return null if it does.
 */
fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
    if (isNullOrEmpty()) null else f(this)


/**
 * A debug utility function to print the value of a variable.
 */
fun <T> T.printIt(prefix: String = ">"): T = also { println("$prefix $this") }

fun <T> T.discardUnless(predicate: (T) -> Boolean): T? = if (predicate(this)) this else null

fun <A, B, C> ((A, B) -> C).curry(): (A) -> (B) -> C = { a: A -> { b: B -> this(a, b) } }

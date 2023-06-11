package com.yonatankarp.zettai.domain.generators

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User
import java.util.*
import kotlin.random.Random

fun randomUser() =
    randomString(lowercase, 3, 6)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        .let(::User)

fun randomToDoList(): ToDoList = ToDoList(
    randomListName(),
    itemsGenerator().take(Random.nextInt(5) + 1).toList()
)

fun randomListName(): ListName =
    randomString(lowercase, 3, 6)
        .let(::ListName)

fun itemsGenerator(): Sequence<ToDoItem> =
    generateSequence {
        randomItem()
    }

fun randomItem() = ToDoItem(randomString(lowercase + digits, 5, 20), null)

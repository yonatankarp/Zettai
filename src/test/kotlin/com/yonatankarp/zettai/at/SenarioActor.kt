package com.yonatankarp.zettai.at

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.User
import org.opentest4j.AssertionFailedError
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

fun createList(listName: String, items: List<String>) =
    ToDoList(ListName(listName), items.map(::ToDoItem))

interface ScenarioActor {
    val name: String
}

class ToDoListOwner(override val name: String) : ScenarioActor {
    fun canSeeTheList(listName: String, items: List<String>, app: ApplicationForAT): Step = {
        val expectedList = createList(listName, items)
        val list = app.getToDoList(name, listName)
        expectThat(list).isEqualTo(expectedList)
    }

    fun cannotSeeTheList(listName: String, app: ApplicationForAT): Step = {
        expectThrows<AssertionFailedError> { app.getToDoList(name, listName) }
    }
}

fun ToDoListOwner.asUser() = User(name)

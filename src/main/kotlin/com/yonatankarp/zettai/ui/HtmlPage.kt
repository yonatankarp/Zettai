package com.yonatankarp.zettai.ui

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HtmlPage(val raw: String)

internal fun List<ToDoItem>.renderItems() =
    joinToString(separator = "", transform = ::renderItem)

private fun renderItem(it: ToDoItem): String = """<tr>
              <td>${it.description}</td>
              <td>${it.dueDate?.toIsoString().orEmpty()}</td>
              <td>${it.status}</td>
            </tr>
""".trimIndent()

private fun LocalDate.toIsoString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

internal fun List<ListName>.render(user: User): String =
    joinToString(separator = "") { renderListName(user, it) }

private fun renderListName(user: User, listName: ListName): String = """<tr>
              <td><a href="${user.name}/${listName.name}">${listName.name}</a></td>
              <td>open</td>
              <td>[archive] [rename] [freeze]</td>
            </tr>
""".trimIndent()

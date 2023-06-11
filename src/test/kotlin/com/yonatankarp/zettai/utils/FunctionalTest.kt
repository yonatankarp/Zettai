package com.yonatankarp.zettai.utils

import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoStatus
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FunctionalTest {
    @Test
    fun `should discard unless`() {
        val itemInProgress = ToDoItem("doing something", status = ToDoStatus.InProgress)
        expectThat(itemInProgress.discardUnless { it.status == ToDoStatus.InProgress }).isEqualTo(itemInProgress)
    }

    @Test
    fun `should discard unless with null`() {
        val itemBlocked = ToDoItem("must do something", status = ToDoStatus.Blocked)
        expectThat(itemBlocked.discardUnless { it.status == ToDoStatus.InProgress }).isEqualTo(null)
    }
}

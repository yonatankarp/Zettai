package com.yonatankarp.zettai.events

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.User
import java.time.Instant

interface EntityState<in E : EntityEvent> {
    fun combine(event: E): EntityState<E>
}

fun Iterable<ToDoListEvent>.fold(): ToDoListState =
    fold(InitialState as ToDoListState) { acc, e -> acc.combine(e) }

sealed class ToDoListState : EntityState<ToDoListEvent> {
    abstract override fun combine(event: ToDoListEvent): ToDoListState
}

object InitialState : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ListCreated -> create(event.id, event.owner, event.name, emptyList())
            else -> this // ignore other events
        }
}
data class ActiveToDoList internal constructor(
    val id: ToDoListId,
    val owner: User,
    val name: ListName,
    val items: List<ToDoItem>,
) : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ItemAdded -> copy(items = items + event.item)
            is ItemRemoved -> copy(items = items - event.item)
            is ItemModified -> copy(items = items - event.prevItem + event.item)
            is ListPutOnHold -> onHold(event.reason)
            is ListClosed -> close(event.closedOn)
            is ListCreated,
            is ListReleased,
            -> this // ignore other events
        }
}

data class OnHoldToDoList internal constructor(
    val id: ToDoListId,
    val owner: User,
    val name: ListName,
    val items: List<ToDoItem>,
    val reason: String,
) : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ListReleased -> release()
            is ListCreated,
            is ItemAdded,
            is ItemRemoved,
            is ItemModified,
            is ListPutOnHold,
            is ListClosed,
            -> this // ignore other events
        }
}

data class ClosedToDoList internal constructor(
    val id: ToDoListId,
    val owner: User,
    val closedOn: Instant,
) : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState = this // ignore other events
}

fun create(id: ToDoListId, owner: User, name: ListName, items: List<ToDoItem>) =
    ActiveToDoList(id, owner, name, items)

fun ActiveToDoList.onHold(reason: String) = OnHoldToDoList(id, owner, name, items, reason)

fun OnHoldToDoList.release() = ActiveToDoList(id, owner, name, items)

fun ActiveToDoList.close(closedOn: Instant) = ClosedToDoList(id, owner, closedOn)

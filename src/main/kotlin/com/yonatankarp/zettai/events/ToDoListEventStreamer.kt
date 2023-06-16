package com.yonatankarp.zettai.events

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.User
import java.util.concurrent.atomic.AtomicReference

typealias EventStreamer<E> = (EntityId) -> List<E>?

typealias EventPersister<E> = (Iterable<E>) -> List<E>

interface ToDoListEventStreamer : EventStreamer<ToDoListEvent> {
    fun retrieveIdFromName(user: User, listName: ListName): ToDoListId?
    fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent>
}

class ToDoListEventStreamerInMemory : ToDoListEventStreamer {

    private val events = AtomicReference<List<ToDoListEvent>>(emptyList())
    override fun retrieveIdFromName(user: User, listName: ListName): ToDoListId? =
        events.get()
            .firstOrNull { it == ListCreated(it.id, user, listName) }
            ?.id

    override fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent> =
        newEvents.toList().also { ne -> events.updateAndGet { it + ne } }

    override fun invoke(id: ToDoListId): List<ToDoListEvent> =
        events.get()
            .filter { it.id == id }
}

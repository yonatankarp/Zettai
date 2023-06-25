package com.yonatankarp.zettai.events

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.User
import java.util.concurrent.atomic.AtomicReference

typealias EventStreamer<E> = (EntityId) -> List<E>?

typealias EventPersister<E> = (Iterable<E>) -> List<E>

typealias FetchStoredEvents<E> = (EventSequence) -> Sequence<StoredEvent<E>>

interface ToDoListEventStreamer : EventStreamer<ToDoListEvent> {
    fun retrieveIdFromName(user: User, listName: ListName): ToDoListId?
    fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent>
    fun fetchAfter(startEvent: EventSequence): Sequence<ToDoListStoredEvent>
}

data class EventSequence(val progressive: Int) {
    operator fun compareTo(other: EventSequence): Int = progressive.compareTo(other.progressive)
}

data class StoredEvent<E : EntityEvent>(val eventSequence: EventSequence, val event: E)

typealias ToDoListStoredEvent = StoredEvent<ToDoListEvent>

class ToDoListEventStreamerInMemory : ToDoListEventStreamer {

    private val events = AtomicReference<List<ToDoListStoredEvent>>(emptyList())

    override fun retrieveIdFromName(user: User, listName: ListName): ToDoListId? =
        events.get()
            .map(ToDoListStoredEvent::event)
            .firstOrNull { it == ListCreated(it.id, user, listName) }
            ?.id

    override fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent> =
        newEvents.toList().also { ne ->
            events.updateAndGet { it + ne.toSavedEvents(it.size) }
        }

    override fun invoke(id: ToDoListId): List<ToDoListEvent> =
        events.get()
            .map(ToDoListStoredEvent::event)
            .filter { it.id == id }

    override fun fetchAfter(startEvent: EventSequence): Sequence<ToDoListStoredEvent> =
        events.get()
            .asSequence()
            .dropWhile { it.eventSequence <= startEvent }

    private fun Iterable<ToDoListEvent>.toSavedEvents(last: Int): List<ToDoListStoredEvent> =
        mapIndexed { index, event ->
            ToDoListStoredEvent(EventSequence(last + index), event)
        }
}

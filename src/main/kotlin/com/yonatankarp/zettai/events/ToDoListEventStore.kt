package com.yonatankarp.zettai.events

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoListRetriever
import com.yonatankarp.zettai.domain.User

class ToDoListEventStore(
    private val eventStreamer: ToDoListEventStreamer,
) : ToDoListRetriever, EventPersister<ToDoListEvent> {
    private fun retrieveById(id: ToDoListId): ToDoListState? =
        eventStreamer(id)
            ?.fold()

    override fun retrieveByName(
        user: User,
        listName: ListName,
    ): ToDoListState? =
        eventStreamer.retrieveIdFromName(user, listName)
            ?.let(::retrieveById)

    override fun invoke(events: Iterable<ToDoListEvent>): List<ToDoListEvent> = eventStreamer.store(events)
}

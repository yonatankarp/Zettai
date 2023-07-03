package com.yonatankarp.zettai.queries

import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoStatus
import com.yonatankarp.zettai.events.EntityId
import com.yonatankarp.zettai.events.FetchStoredEvents
import com.yonatankarp.zettai.events.ItemAdded
import com.yonatankarp.zettai.events.ItemModified
import com.yonatankarp.zettai.events.ItemRemoved
import com.yonatankarp.zettai.events.ListClosed
import com.yonatankarp.zettai.events.ListCreated
import com.yonatankarp.zettai.events.ListPutOnHold
import com.yonatankarp.zettai.events.ListReleased
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.queries.projections.ConcurrentMapProjection
import com.yonatankarp.zettai.queries.projections.CreateRow
import com.yonatankarp.zettai.queries.projections.DeleteRow
import com.yonatankarp.zettai.queries.projections.DeltaRow
import com.yonatankarp.zettai.queries.projections.InMemoryProjection
import com.yonatankarp.zettai.queries.projections.RowId

data class ItemProjectionRow(val item: ToDoItem, val listId: EntityId)

class ToDoItemProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ItemProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        eventFetcher,
        ::eventProjector,
    ) {

    fun findWhatsNext(maxRows: Int, lists: List<EntityId>): List<ItemProjectionRow> =
        allRows()
            .values
            .filter { it.listId in lists }
            .filter { it.item.dueDate != null && it.item.status == ToDoStatus.Todo }
            .sortedBy { it.item.dueDate }
            .take(maxRows)

    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ItemProjectionRow>> =
            when (e) {
                is ItemAdded -> e.createRow()
                is ItemRemoved -> e.deleteRow()
                is ItemModified -> e.updateRow()
                is ListCreated,
                is ListPutOnHold,
                is ListReleased,
                is ListClosed,
                -> emptyList() // Ignore events that are not involved with items
            }
    }
}

private fun ToDoListEvent.itemRowId(item: ToDoItem): RowId = RowId("${id}_${item.description}")

private fun ItemAdded.createRow() = CreateRow(itemRowId(item), ItemProjectionRow(item, id)).toSingle()
private fun ItemRemoved.deleteRow() = DeleteRow<ItemProjectionRow>(itemRowId(item)).toSingle()
private fun ItemModified.updateRow() =
    listOf(
        CreateRow(itemRowId(item), ItemProjectionRow(item, id)),
        DeleteRow(itemRowId(prevItem)),
    )

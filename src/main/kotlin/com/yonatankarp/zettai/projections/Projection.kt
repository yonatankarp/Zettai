package com.yonatankarp.zettai.projections

import com.yonatankarp.zettai.events.EntityEvent
import com.yonatankarp.zettai.events.EventSequence
import com.yonatankarp.zettai.events.FetchStoredEvents

typealias ProjectEvents<R, E> = (E) -> List<DeltaRow<R>>

interface Projection<R : Any, E : EntityEvent> {
    val eventProjector: ProjectEvents<R, E>

    val eventFetcher: FetchStoredEvents<E>

    fun lastProjectedEvent(): EventSequence

    fun update() {
        eventFetcher(lastProjectedEvent())
            .forEach { storedEvent ->
                applyDelta(storedEvent.eventSequence, eventProjector(storedEvent.event))
            }
    }

    fun applyDelta(
        eventSequence: EventSequence,
        deltas: List<DeltaRow<R>>,
    )
}

data class RowId(val id: String)

sealed class DeltaRow<R : Any>

data class CreateRow<R : Any>(val rowId: RowId, val row: R) : DeltaRow<R>()

data class DeleteRow<R : Any>(val rowId: RowId) : DeltaRow<R>()

data class UpdateRow<R : Any>(val rowId: RowId, val updateRow: R.() -> R) : DeltaRow<R>()

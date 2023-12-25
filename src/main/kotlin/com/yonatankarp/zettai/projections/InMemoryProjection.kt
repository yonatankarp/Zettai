package com.yonatankarp.zettai.projections

import com.yonatankarp.zettai.events.EntityEvent
import com.yonatankarp.zettai.events.EventSequence
import com.yonatankarp.zettai.events.FetchStoredEvents
import java.util.concurrent.atomic.AtomicReference

interface InMemoryProjection<R : Any, E : EntityEvent> : Projection<R, E> {
    fun allRows(): Map<RowId, R>
}

data class ConcurrentMapProjection<R : Any, E : EntityEvent>(
    override val eventFetcher: FetchStoredEvents<E>,
    override val eventProjector: ProjectEvents<R, E>,
) : InMemoryProjection<R, E> {
    private val rowsReference = AtomicReference(emptyMap<RowId, R>())

    private val lastEventReference = AtomicReference(EventSequence(-1))

    override fun allRows(): Map<RowId, R> = rowsReference.get()

    override fun lastProjectedEvent(): EventSequence = lastEventReference.get()

    override fun applyDelta(
        eventSequence: EventSequence,
        deltas: List<DeltaRow<R>>,
    ) {
        deltas.forEach { delta ->
            rowsReference.getAndUpdate { rows ->
                when (delta) {
                    is CreateRow -> rows.createRow(delta)
                    is DeleteRow -> rows.deleteRow(delta)
                    is UpdateRow -> rows.updateRow(delta)
                }
            }
        }.also { lastEventReference.getAndSet(eventSequence) }
    }

    private fun Map<RowId, R>.createRow(delta: CreateRow<R>) = this + (delta.rowId to delta.row)

    private fun Map<RowId, R>.deleteRow(delta: DeleteRow<R>) = this - delta.rowId

    private fun Map<RowId, R>.updateRow(delta: UpdateRow<R>) =
        this[delta.rowId]
            ?.let { oldRow ->
                this - delta.rowId + (delta.rowId to delta.updateRow(oldRow))
            }
}

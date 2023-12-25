package com.yonatankarp.zettai.projections

import com.yonatankarp.zettai.events.FetchStoredEvents
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.queries.ToDoListProjection

class ToDoListQueryRunner(eventFetcher: FetchStoredEvents<ToDoListEvent>) : QueryRunner<ToDoListQueryRunner> {
    internal val listProjection = ToDoListProjection(eventFetcher)

    override fun <R> invoke(f: ToDoListQueryRunner.() -> R): ProjectionQuery<R> = ProjectionQuery(setOf(listProjection)) { f(this) }
}

package com.yonatankarp.zettai.queries

import com.yonatankarp.zettai.events.FetchStoredEvents
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.queries.projections.ProjectionQuery
import com.yonatankarp.zettai.queries.projections.QueryRunner

class ToDoListQueryRunner(eventFetcher: FetchStoredEvents<ToDoListEvent>) : QueryRunner<ToDoListQueryRunner> {
    internal val listProjection = ToDoListProjection(eventFetcher)
    internal val itemProjection = ToDoItemProjection(eventFetcher)

    override fun <R> invoke(f: ToDoListQueryRunner.() -> R): ProjectionQuery<R> =
        ProjectionQuery(setOf(listProjection, itemProjection)) { f(this) }
}

package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.commands.ToDoListCommandHandler
import com.yonatankarp.zettai.events.ToDoListEventStore
import com.yonatankarp.zettai.events.ToDoListEventStreamerInMemory
import com.yonatankarp.zettai.projections.ToDoListQueryRunner

fun prepareToDoListHubForTests(): ToDoListHub {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore)
    val queryRunner = ToDoListQueryRunner(streamer::fetchAfter)
    return ToDoListHub(queryRunner, cmdHandler, eventStore)
}

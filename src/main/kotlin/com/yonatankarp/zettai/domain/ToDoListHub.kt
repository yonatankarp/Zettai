package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.commands.ToDoListCommand
import com.yonatankarp.zettai.commands.ToDoListCommandHandler
import com.yonatankarp.zettai.events.EventPersister
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.projections.ToDoListQueryRunner
import com.yonatankarp.zettai.utils.Outcome
import com.yonatankarp.zettai.utils.failIfNull

typealias ZettaiOutcome<T> = Outcome<ZettaiError, T>

interface ZettaiHub {
    fun getList(
        user: User,
        listName: ListName,
    ): ZettaiOutcome<ToDoList>

    fun getLists(user: User): ZettaiOutcome<List<ListName>>

    fun handle(command: ToDoListCommand): ZettaiOutcome<ToDoListCommand>
}

class ToDoListHub(
    private val queryRunner: ToDoListQueryRunner,
    private val commandHandler: ToDoListCommandHandler,
    private val persistEvents: EventPersister<ToDoListEvent>,
) : ZettaiHub {
    override fun getList(
        user: User,
        listName: ListName,
    ): ZettaiOutcome<ToDoList> =
        queryRunner {
            listProjection
                .findList(user, listName)
                .failIfNull(InvalidRequestError("List $listName of user $user not found!"))
        }.execute()

    override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
        queryRunner {
            listProjection
                .findAll(user)
                .failIfNull(InvalidRequestError("User $user not found!"))
                .transform { it.toList() }
        }.execute()

    override fun handle(command: ToDoListCommand): ZettaiOutcome<ToDoListCommand> =
        commandHandler(command)
            .transform(persistEvents)
            .transform { command }
}

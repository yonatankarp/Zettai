package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.commands.ToDoListCommand
import com.yonatankarp.zettai.commands.ToDoListCommandHandler
import com.yonatankarp.zettai.events.EventPersister
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.utils.Outcome
import com.yonatankarp.zettai.utils.failIfNull

typealias ZettaiOutcome<T> = Outcome<ZettaiError, T>

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList>
    fun getLists(user: User): ZettaiOutcome<List<ListName>>
    fun handle(command: ToDoListCommand): ZettaiOutcome<ToDoListCommand>
}

class ToDoListHub(
    private val fetcher: ToDoListFetcher,
    private val commandHandler: ToDoListCommandHandler,
    private val persistEvents: EventPersister<ToDoListEvent>,
) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList> =
        fetcher.get(user, listName)
            .failIfNull(InvalidRequestError("List $listName of user $user not found!"))

    override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
        fetcher
            .getAll(user)
            .failIfNull(InvalidRequestError("User $user not found!"))
            .transform { it.toList() }

    override fun handle(command: ToDoListCommand): ZettaiOutcome<ToDoListCommand> =
        commandHandler(command)
            .transform(persistEvents)
            .transform { command }
}

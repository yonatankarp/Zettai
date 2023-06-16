package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.commands.ToDoListCommand
import com.yonatankarp.zettai.commands.ToDoListCommandHandler
import com.yonatankarp.zettai.events.EventPersister
import com.yonatankarp.zettai.events.ToDoListEvent

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
    fun getLists(user: User): List<ListName>?
    fun handle(command: ToDoListCommand): ToDoListCommand?
}

class ToDoListHub(
    private val fetcher: ToDoListFetcher,
    private val commandHandler: ToDoListCommandHandler,
    private val persistEvents: EventPersister<ToDoListEvent>,
) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ToDoList? = fetcher.get(user, listName)

    override fun getLists(user: User): List<ListName>? = fetcher.getAll(user)

    override fun handle(command: ToDoListCommand): ToDoListCommand? =
        commandHandler(command)
            ?.let(persistEvents)
            ?.let { command }
}

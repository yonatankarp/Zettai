package com.yonatankarp.zettai.commands

import com.yonatankarp.zettai.domain.InconsistentStateError
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.ToDoListCommandError
import com.yonatankarp.zettai.domain.ToDoListRetriever
import com.yonatankarp.zettai.domain.ToDoListUpdatableFetcher
import com.yonatankarp.zettai.domain.ZettaiError
import com.yonatankarp.zettai.domain.ZettaiOutcome
import com.yonatankarp.zettai.events.ActiveToDoList
import com.yonatankarp.zettai.events.ClosedToDoList
import com.yonatankarp.zettai.events.InitialState
import com.yonatankarp.zettai.events.ItemAdded
import com.yonatankarp.zettai.events.ListCreated
import com.yonatankarp.zettai.events.OnHoldToDoList
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.utils.Outcome
import com.yonatankarp.zettai.utils.asFailure
import com.yonatankarp.zettai.utils.asSuccess

typealias CommandHandler<COMMAND, EVENT, ERROR> = (COMMAND) -> Outcome<ERROR, List<EVENT>>

typealias ToDoListCommandOutcome = ZettaiOutcome<List<ToDoListEvent>>

class ToDoListCommandHandler(
    private val entityRetriever: ToDoListRetriever,
    private val readModel: ToDoListUpdatableFetcher, // temporary needed to update the read model
) : CommandHandler<ToDoListCommand, ToDoListEvent, ZettaiError> {
    override fun invoke(command: ToDoListCommand): ToDoListCommandOutcome =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): ToDoListCommandOutcome =
        when (val listState = entityRetriever.retrieveByName(user, name) ?: InitialState) {
            InitialState -> {
                readModel.assignListToUser(user, ToDoList(name, emptyList()))
                ListCreated(id, user, name).asCommandSuccess()
            }

            is ActiveToDoList,
            is OnHoldToDoList,
            is ClosedToDoList -> InconsistentStateError(this, listState).asFailure()
        }

    private fun AddToDoItem.execute(): ToDoListCommandOutcome =
        when (val listState = entityRetriever.retrieveByName(user, name)) {
            is ActiveToDoList -> {
                if (listState.items.any { it.description == item.description })
                    ToDoListCommandError("cannot have 2 items with same name").asFailure()
                else {
                    readModel.addItemToList(user, listState.name, item)
                    ItemAdded(listState.id, item).asCommandSuccess()
                }
            }

            InitialState,
            is OnHoldToDoList,
            is ClosedToDoList -> InconsistentStateError(this, listState).asFailure()

            null -> ToDoListCommandError("list $name not found").asFailure()
        }

    private fun ToDoListEvent.asCommandSuccess(): ToDoListCommandOutcome = listOf(this).asSuccess()
}

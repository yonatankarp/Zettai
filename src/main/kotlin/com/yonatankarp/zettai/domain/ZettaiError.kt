package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.commands.ToDoListCommand
import com.yonatankarp.zettai.events.ToDoListState
import com.yonatankarp.zettai.utils.OutcomeError

sealed class ZettaiError : OutcomeError
data class InvalidRequestError(override val msg: String) : ZettaiError()
data class ToDoListCommandError(override val msg: String) : ZettaiError()
data class InconsistentStateError(val command: ToDoListCommand, val state: ToDoListState) : ZettaiError() {
    override val msg = "Command $command cannot be applied to state $state"
}

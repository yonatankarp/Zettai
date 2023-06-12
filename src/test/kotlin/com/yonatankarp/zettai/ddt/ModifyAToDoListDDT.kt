package com.yonatankarp.zettai.ddt

import com.ubertob.pesticide.core.DDT
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner

class ModifyAToDoListDDT : AbstractDDT() {

    private val ann by NamedActor(::ToDoListOwner)

    @DDT
    fun `the list owner can add new items`() = ddtScenario {
        setUp {
            ann.`starts with a list`("diy", emptyList())
        }.thenPlay(
            ann.`can add #item to #listname`("paint the shelf", "diy"),
            ann.`can add #item to #listname`("fix the gate", "diy"),
            ann.`can add #item to #listname`("change the lock", "diy"),
            ann.`can see #listname with #itemnames`("diy", listOf("paint the shelf", "fix the gate", "change the lock"))
        )
    }
}

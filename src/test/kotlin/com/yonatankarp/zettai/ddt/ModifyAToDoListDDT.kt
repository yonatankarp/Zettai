@file:Suppress("ktlint:standard:function-naming")

package com.yonatankarp.zettai.ddt

import com.ubertob.pesticide.core.DDT
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner

class ModifyAToDoListDDT : AbstractDDT() {
    private val ann by NamedActor(::ToDoListOwner)

    @DDT
    fun `users can create a new list`() =
        ddtScenario {
            play(
                ann.`can create a new list called #listname`("mylist"),
                ann.`can see #listname with #itemnames`("mylist", emptyList()),
            )
        }

    @DDT
    fun `the list owner can add new items`() =
        ddtScenario {
            setUp {
                ann.`starts with a list`("diy", emptyList())
            }.thenPlay(
                ann.`can add #item to #listname`("paint the shelf", "diy"),
                ann.`can add #item to #listname`("fix the gate", "diy"),
                ann.`can add #item to #listname`("change the lock", "diy"),
                ann.`can see #listname with #itemnames`(
                    "diy",
                    listOf(
                        "fix the gate",
                        "paint the shelf",
                        "change the lock",
                    ),
                ),
            )
        }
}

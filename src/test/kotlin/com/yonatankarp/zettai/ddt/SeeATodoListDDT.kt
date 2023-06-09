package com.yonatankarp.zettai.ddt

import com.ubertob.pesticide.core.*
import com.yonatankarp.zettai.ddt.actions.DomainOnlyActions
import com.yonatankarp.zettai.ddt.actions.HttpActions
import com.yonatankarp.zettai.ddt.actions.ZettaiActions
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

fun allActions() = setOf(
    DomainOnlyActions(),
    HttpActions()
)

class SeeATodoListDDT : ZettaiDDT(allActions()) {

    private val frank by NamedActor(::ToDoListOwner)
    private val bob by NamedActor(::ToDoListOwner)

    private val shoppingListName = "shopping"
    private val shoppingItems = listOf("carrots", "apples", "milk")

    private val gardenListName = "gardening"
    private val gardenItems = listOf("fix the fence", "mowing the lawn")

    @DDT
    fun `List owners can see their lists`() = ddtScenario {
        setUp {
            frank.`starts with a list`(shoppingListName, shoppingItems)
            bob.`starts with a list`(gardenListName, gardenItems)
        }.thenPlay(
            frank.`can see #listname with #itemnames`(shoppingListName, shoppingItems),
            bob.`can see #listname with #itemnames`(gardenListName, gardenItems)
        )
    }

    @DDT
    fun `Only owners can see their lists`() = ddtScenario {
        setUp {
            frank.`starts with a list`(shoppingListName, shoppingItems)
            bob.`starts with a list`(gardenListName, gardenItems)
        }.thenPlay(
            frank.`cannot see #listname`(gardenListName),
            bob.`cannot see #listname`(shoppingListName)
        )
    }
}

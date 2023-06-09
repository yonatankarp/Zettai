package com.yonatankarp.zettai.at

import com.yonatankarp.zettai.domain.ToDoListHub
import org.junit.jupiter.api.Test

class SeeATodoListAT {

    private val frank = ToDoListOwner("Frank")
    private val shoppingItems = listOf("carrots", "apples", "milk")
    private val frankList = createList("shopping", shoppingItems)

    private val bob = ToDoListOwner("Bob")
    private val gardenItems = listOf("fix the fence", "mowing the lawn")
    private val bobList = createList("gardening", gardenItems)

    private val hub = ToDoListHub(
        mapOf(
            frank.asUser() to listOf(frankList),
            bob.asUser() to listOf(bobList)
        )
    )

    @Test
    fun `List owners can see their lists`() {
        val app = startTheApplication(hub)
        app.runScenario(
            frank.canSeeTheList("shopping", shoppingItems, app),
            bob.canSeeTheList("gardening", gardenItems, app)
        )
    }

    @Test
    fun `Only owners can see their lists`() {
        val app = startTheApplication(hub)
        app.runScenario(
            frank.cannotSeeTheList("gardening", app),
            bob.cannotSeeTheList("shopping", app)
        )
    }
}

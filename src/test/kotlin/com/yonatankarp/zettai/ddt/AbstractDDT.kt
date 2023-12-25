package com.yonatankarp.zettai.ddt

import com.ubertob.pesticide.core.DomainDrivenTest
import com.yonatankarp.zettai.ddt.actions.DomainOnlyActions
import com.yonatankarp.zettai.ddt.actions.HttpActions
import com.yonatankarp.zettai.ddt.actions.ZettaiActions

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

internal val allActions: Set<ZettaiActions>
    get() =
        setOf(
            DomainOnlyActions(),
            HttpActions(),
        )

@Suppress("unused")
internal val domainOnlyActions: Set<ZettaiActions>
    get() = setOf(DomainOnlyActions())

@Suppress("unused")
internal val httpActions: Set<ZettaiActions>
    get() = setOf(HttpActions())

abstract class AbstractDDT(actions: Set<ZettaiActions> = allActions) : ZettaiDDT(actions)

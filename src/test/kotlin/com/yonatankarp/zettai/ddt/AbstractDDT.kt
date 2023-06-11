package com.yonatankarp.zettai.ddt

import com.ubertob.pesticide.core.DomainDrivenTest
import com.yonatankarp.zettai.ddt.actions.DomainOnlyActions
import com.yonatankarp.zettai.ddt.actions.HttpActions
import com.yonatankarp.zettai.ddt.actions.ZettaiActions

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

fun allActions() = setOf(
    DomainOnlyActions(),
    HttpActions()
)

abstract class AbstractDDT : ZettaiDDT(allActions())

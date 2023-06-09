package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User

interface ZettaiActions: DdtActions<DdtProtocol> {
    fun getToDoList(user: User, listName: ListName): ToDoList?
}

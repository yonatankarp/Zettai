package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.*
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.ToDoListHub
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.webservice.Zettai
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.server.Jetty
import org.http4k.server.asServer

class HttpActions(private val env: String = "local") : ZettaiActions {

    override val protocol: DdtProtocol = Http(env)

    private val hub = ToDoListHub(emptyMap())

    private val zettaiPort = 8000 // different from the one on main
    private val server = Zettai(hub).asServer(Jetty(zettaiPort))

    private val client = JettyClient()

    override fun prepare(): DomainSetUp {
        server.start()
        return Ready
    }

    override fun tearDown(): DdtActions<DdtProtocol> = also { server.stop() }

    private fun callZettai(method: Method, path: String): Response =
        client(log(Request(
            method, "http://localhost:$zettaiPort/$path")
        ))

    override fun getToDoList(user: User, listName: ListName): ToDoList? = TODO("Not yet implemented")
}

// TODO: use real logger
fun <T> log(something: T): T = something.also { println("--- $something") }

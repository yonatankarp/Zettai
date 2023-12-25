package com.yonatankarp.zettai.projections

interface QueryRunner<Self : QueryRunner<Self>> {
    operator fun <R> invoke(f: Self.() -> R): ProjectionQuery<R>
}

data class ProjectionQuery<T>(
    val projections: Set<Projection<*, *>>,
    val runner: () -> T,
) {
    fun <U> transform(f: (T) -> U): ProjectionQuery<U> = ProjectionQuery(projections) { f(runner()) }

    fun execute(): T {
        projections.forEach(Projection<*, *>::update)
        return runner()
    }
}

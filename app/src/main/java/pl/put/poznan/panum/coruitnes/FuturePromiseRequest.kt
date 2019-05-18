package pl.put.poznan.panum.coruitnes

import java9.util.concurrent.CompletableFuture
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

val cores = Runtime.getRuntime().availableProcessors()
val executor: ExecutorService = Executors.newFixedThreadPool(cores + 1)

fun futureRequestRepos(user: String): Future<List<Repo>?> {
    return executor.submit(Callable { requestRepos(user) })
}

fun futureRequestDetails(user: String, repo: String): Future<Repo?> {
    return executor.submit(Callable { requestDetails(user, repo) })
}

fun promiseRequestRepos(user: String): CompletableFuture<List<Repo>?> {
    return CompletableFuture.supplyAsync { requestRepos(user) }
}

fun promiseRequestDetails(user: String, repo: String): CompletableFuture<Repo?>? {
    return CompletableFuture.supplyAsync { requestDetails(user, repo) }
}
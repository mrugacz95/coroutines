package pl.put.poznan.panum.coruitnes

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import java.util.concurrent.*

val cores = Runtime.getRuntime().availableProcessors()
val executor: ExecutorService = Executors.newFixedThreadPool(cores + 1)

class MainThreadExecutor : Executor {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun execute(@NonNull command: Runnable) {
        mainThreadHandler.post(command)
    }
}

val mainThreadExecutor = MainThreadExecutor()

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
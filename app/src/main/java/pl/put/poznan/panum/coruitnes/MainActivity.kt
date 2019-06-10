package pl.put.poznan.panum.coruitnes

import android.os.Bundle
import android.os.NetworkOnMainThreadException
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Runnable
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {

    val user = "mrugacz95"
    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)
    private val gitHubApiServce by lazy {
        GitHubApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt_on_main_thread.setOnClickListener {
            try {
                displayRepos(requestRepos(user))
            } catch (e: NetworkOnMainThreadException) {
                tv_repos.text = e.toString()
            }
        }
        bt_clear.setOnClickListener {
            tv_repos.text = ""
            tv_repo_details.text = ""
        }
        bt_threads.setOnClickListener {
            Thread(Runnable {
                val repos = requestRepos(user)
                runOnUiThread {
                    displayRepos(repos)
                }
                val repoName = repos?.get(0)?.name ?: return@Runnable
                val details = requestDetails(user, repoName)
                runOnUiThread {
                    displayDetails(details)
                }
            }).start()
        }
        bt_callbacks.setOnClickListener {
            AsyncTaskWithCallbackReposRequest(user) { repos ->
                displayRepos(repos)
                val repoName = repos?.get(0)?.name
                AsyncTaskWithCallbackDetailsRequest(
                    user,
                    repoName ?: return@AsyncTaskWithCallbackReposRequest
                ) { details ->
                    displayDetails(details)
                }.execute()
            }.execute()
        }
        bt_future.setOnClickListener {
            executor.submit {
                val repos = futureRequestRepos(user).get()
                mainThreadExecutor.execute {
                    displayRepos(repos)
                }
                val repoName = repos?.get(0)?.name ?: return@submit
                val topics = futureRequestDetails(user, repoName).get()
                mainThreadExecutor.execute {
                    displayDetails(topics)
                }
            }
        }
        bt_promise.setOnClickListener {
            val repositories = promiseRequestRepos(user)
            repositories
                .thenAcceptAsync(
                    Consumer { t -> displayRepos(t) },
                    mainThreadExecutor
                )
            repositories
                .thenApplyAsync { repos ->
                    repos?.get(0)?.name ?: throw NullPointerException()
                }
                .thenApply { repoName -> promiseRequestDetails(user, repoName) }
                .thenAcceptAsync(
                    Consumer { repo -> displayDetails(repo?.get()) },
                    mainThreadExecutor
                )


        }
        bt_rx.setOnClickListener {
            val repositories = Observable.just(user)
                .map { user -> requestRepos(user) }
                .subscribeOn(Schedulers.io())
            repositories
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { repos ->
                        displayRepos(repos)
                    },
                    { e -> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show() }
                )
            repositories
                .map { repos ->
                    val repoName = repos[0].name
                    requestDetails(user, repoName)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { repo ->
                        displayDetails(repo)
                    },
                    { e -> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show() }
                )
        }
        bt_coroutines.setOnClickListener {
            ioScope.launch {
                val repos = requestRepos(user)
                MainScope().launch {
                    displayRepos(repos)
                }
                val repoName = repos?.get(0)?.name ?: return@launch
                val details = requestDetails(user, repoName)
                MainScope().launch {
                    displayDetails(details)
                }
            }
        }
        bt_retrofit.setOnClickListener {
            gitHubApiServce.getRepos(user).enqueue(object : Callback<List<Repo>> {
                override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                    tv_repos.text = t.message
                }

                override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                    if (!response.isSuccessful) {
                        tv_repos.text = response.errorBody().toString()
                        return
                    }
                    val repos = response.body() ?: return
                    displayRepos(repos)
                    val repoName = repos[0].name
                    gitHubApiServce.getDetails(user, repoName).enqueue(object : Callback<Repo> {
                        override fun onFailure(call: Call<Repo>, t: Throwable) {
                            tv_repo_details.text = t.message
                        }

                        override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                            if (!response.isSuccessful) {
                                tv_repo_details.text = response.errorBody().toString()
                                return
                            }
                            displayDetails(response.body())
                        }

                    })

                }

            })
        }
        bt_retrofit_futures.setOnClickListener {

            val repositories = gitHubApiServce
                .getReposWithCompletableFuture(user)
            repositories
                .thenAcceptAsync(
                    Consumer { repos -> displayRepos(repos) }, mainThreadExecutor
                )
            repositories
                .thenApplyAsync { repos ->
                    repos?.get(0)?.name ?: throw NullPointerException()
                }
                .thenApply { repoName -> promiseRequestDetails(user, repoName) }
                .thenAcceptAsync(
                    Consumer { repo -> displayDetails(repo?.get()) },
                    mainThreadExecutor
                )
        }
        bt_retrofit_rx.setOnClickListener {
            val repositories = gitHubApiServce.getReposRx(user).subscribeOn(Schedulers.io())
            repositories
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ repos -> displayRepos(repos) },
                    { e -> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show() })
            repositories
                .map { repos ->
                    repos[0].name
                }
                .flatMap { repoName ->
                    gitHubApiServce.getDetailsRx(user, repoName)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { repo -> displayDetails(repo) },
                    { e -> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show() })
        }
        bt_retrofit_coroutines.setOnClickListener {
            ioScope.launch {
                val repos = gitHubApiServce.getReposWithCoroutines(user).await()
                MainScope().launch {
                    displayRepos(repos)
                }
                val repoName = repos[0].name
                val details = gitHubApiServce.getDetailsWithCoroutines(user, repoName).await()
                MainScope().launch {
                    displayDetails(details)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        MainScope().coroutineContext.cancelChildren()
        ioScope.coroutineContext.cancelChildren()
    }

    private fun displayRepos(repos: List<Repo>?) {
        tv_repos.text = repos?.joinToString(",\n")
    }

    private fun displayDetails(repo: Repo?) {
        tv_repo_details.text = getString(R.string.details, repo?.name, repo?.description, repo?.language)
    }
}

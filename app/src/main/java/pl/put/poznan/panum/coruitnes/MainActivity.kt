package pl.put.poznan.panum.coruitnes

import android.os.Bundle
import android.os.NetworkOnMainThreadException
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val user = "mrugacz95"

    private val gitHubApiServe by lazy {
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
        bt_retrofit.setOnClickListener {
            retrofitRequest()
        }
        bt_future.setOnClickListener {
            val repos = futureRequestRepos(user).get()
            displayRepos(repos)
            val repoName = repos?.get(0)?.name ?: return@setOnClickListener
            val topics = futureRequestDetails(user, repoName)
            displayDetails(topics.get())
        }
        bt_promise.setOnClickListener {
            val repositories = promiseRequestRepos(user)
            repositories.thenAcceptAsync { repos ->
                displayRepos(repos)
            }
            repositories
                .thenApply { repos ->
                    repos?.get(0)?.name ?: throw NullPointerException()
                }
                .thenApply { repoName -> promiseRequestDetails(user, repoName) }
                .exceptionallyAsync { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                    null
                }
                .thenAcceptAsync { repo ->
                    displayDetails(repo?.get())
                }

        }
        bt_rx.setOnClickListener {
            val repositories = Observable.just(user)
                .map { user -> requestRepos(user) }
                .subscribeOn(Schedulers.io())
            repositories
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repos ->
                    displayRepos(repos)
                }
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
    }

    private fun displayRepos(repos: List<Repo>?) {
        tv_repos.text = repos?.joinToString(",\n")
    }

    private fun displayDetails(repo: Repo?) {
        tv_repo_details.text = getString(R.string.details, repo?.name, repo?.description, repo?.language)
    }

    private fun retrofitRequest() {
        gitHubApiServe.listRepos(user).enqueue(object : Callback<List<Repo>> {
            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                tv_repos.text = t.message
            }

            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    displayRepos(response.body())
                } else {
                    tv_repos.text = response.errorBody().toString()
                }
            }

        })
    }
}

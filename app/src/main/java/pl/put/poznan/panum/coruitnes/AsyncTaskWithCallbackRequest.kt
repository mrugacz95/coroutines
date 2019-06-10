package pl.put.poznan.panum.coruitnes

import android.os.AsyncTask

class AsyncTaskWithCallbackReposRequest(private val user: String, private val callback: (List<Repo>?) -> Unit) :
    AsyncTask<Void, Void, List<Repo>?>() {
    override fun doInBackground(vararg params: Void?): List<Repo>? {
        return requestRepos(user)
    }

    override fun onPostExecute(result: List<Repo>?) {
        callback.invoke(result)
    }
}

class AsyncTaskWithCallbackDetailsRequest(
    private val user: String,
    private val repo: String,
    private val callback: (Repo?) -> Unit
) :
    AsyncTask<Void, Void, Repo>() {
    override fun doInBackground(vararg params: Void?): Repo? {
        return requestDetails(user, repo)
    }

    override fun onPostExecute(result: Repo?) {
        callback.invoke(result)
    }
}
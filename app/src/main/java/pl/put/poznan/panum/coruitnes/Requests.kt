package pl.put.poznan.panum.coruitnes

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL

const val BASE_URL = "https://api.github.com"

fun <T> httpRequest(url: String, type: Type): T? {
    val requestUrl = URL(url)
    val conn = requestUrl.openConnection() as HttpURLConnection
    conn.connect()
    if (conn.responseCode == 200) {
        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        return Gson().fromJson<T>(reader, type)
    }
    return null
}

fun requestRepos(user: String): List<Repo>? {
    return httpRequest<List<Repo>>(
        "$BASE_URL/users/$user/repos?sort=updated&direction=desc",
        object : TypeToken<List<Repo>>() {}.type
    )
}

fun requestDetails(user: String, repo: String): Repo? {
    return httpRequest<Repo>(
        "$BASE_URL/repos/$user/$repo",
        Repo::class.java
    )
}
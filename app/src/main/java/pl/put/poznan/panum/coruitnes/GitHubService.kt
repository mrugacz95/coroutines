package pl.put.poznan.panum.coruitnes

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import io.reactivex.Observable
import java9.util.concurrent.CompletableFuture
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApiService {

    companion object {
        fun create(): GitHubApiService {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .baseUrl("https://api.github.com/")
                .build()

            return retrofit.create(GitHubApiService::class.java)
        }
    }

    @GET("users/{user}/repos?sort=updated&direction=desc")
    fun getRepos(@Path("user") user: String): Call<List<Repo>>

    @GET("repos/{user}/{repo}")
    fun getDetails(
        @Path("user") user: String,
        @Path("repo") repo: String
    ): Call<Repo>

    @GET("users/{user}/repos?sort=updated&direction=desc")
    fun getReposRx(@Path("user") user: String): Observable<List<Repo>>

    @GET("repos/{user}/{repo}")
    fun getDetailsRx(
        @Path("user") user: String,
        @Path("repo") repo: String
    ): Observable<Repo>

    @GET("users/{user}/repos?sort=updated&direction=desc")
    suspend fun getReposWithCoroutines(@Path("user") user: String): Deferred<List<Repo>>

    @GET("repos/{user}/{repo}")
    fun getDetailsWithCoroutines(
        @Path("user") user: String,
        @Path("repo") repo: String
    ): Deferred<Repo>

    @GET("users/{user}/repos?sort=updated&direction=desc")
    suspend fun getReposWithCompletableFuture(@Path("user") user: String): CompletableFuture<List<Repo>>

    @GET("repos/{user}/{repo}")
    fun getDetailsWithCompletableFuture(
        @Path("user") user: String,
        @Path("repo") repo: String
    ): CompletableFuture<Repo>
}
package pl.put.poznan.panum.coruitnes

import retrofit2.Call
import retrofit2.Retrofit
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
                .baseUrl("https://api.github.com/")
                .build()

            return retrofit.create(GitHubApiService::class.java)
        }
    }

    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): Call<List<Repo>>

    @GET("{user}/{repo}")
    fun listRepos(
        @Path("user") user: String,
        @Path("repo") repo: String
    ): Call<List<Repo>>
}
package pl.put.poznan.panum.coruitnes

import com.google.gson.annotations.SerializedName


data class Repo(
    @SerializedName("name") val name: String,
    @SerializedName("topics") val topics: List<String>,
    @SerializedName("description") val description: String,
    @SerializedName("language") val language: String
)
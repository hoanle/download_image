package com.example.downloadimage

import io.reactivex.rxjava3.core.Single
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ApiManager {

    fun getMovieFromRemote(): Single<Movie> {
        return Single.create {
            val jsonString = """
            {'title':'Civil War',
            'image':['http://movie.phinf.naver.net/20151127_84/1448585272016tiBsF_JPEG/movie_image.jpg?type=m665_443_2',
            'http://movie.phinf.naver.net/20151125_36/1448434523214fPmj0_JPEG/movie_image.jpg?type=m665_443_2', 
            'https://upload.wikimedia.org/wikipedia/commons/7/7a/Salzburg_from_Gaisberg_big_version.jpg','']}
            """.trimIndent()
            try {
                val jsonObject = JSONObject(jsonString)
                val title = jsonObject["title"] as String
                val imageArray = jsonObject["image"] as JSONArray
                val images = arrayListOf<String>()

                for (i in 0 until imageArray.length()) {
                    val item = imageArray.getString(i)
                    images.add(item)
                }
                it.onSuccess(Movie(title, images))
            } catch (e: JSONException) {
                e.printStackTrace()
                it.onError(Throwable(e.localizedMessage))
            }
        }

    }
}
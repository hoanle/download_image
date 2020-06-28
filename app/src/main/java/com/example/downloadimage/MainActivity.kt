package com.example.downloadimage

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val apiManager by lazy { ApiManager() }
    private val imageManager by lazy { ImageManager(this) }
    private var movie: Movie? = null
    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //The case when that movie is unchanged in a short time, api does not need to reload frequently
        loadMovie()
    }

    //When user taps on image, movie to next position in the list. Return to first position if no more item
    fun onNextImage(view: View) {
        movie?.let {
            tvProgress.text = getString(R.string.loading)
            view.visibility = View.GONE
            if (currentPosition == it.images.size - 1) {
                currentPosition = 0
            } else {
                currentPosition += 1
            }
            imageManager.displayImage(it.images[currentPosition], ivImage)
        }
    }

    /**
     * Function to load movie and then display to UI
     */
    private fun loadMovie() {
        apiManager.getMovieFromRemote()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                movie = it
                tvTitle.text = it.title
                onFinishLoadingMovie()
            }, {
                tvTitle.text = getString(R.string.error)
            })
    }

    /**
     * When finish loading movie, subscribe to downloadListener to get update from it
     */
    private fun onFinishLoadingMovie() {
        movie?.let {
            if (it.images.isNotEmpty()) {
                imageManager
                    .displayImage(it.images[0], ivImage)
                    .map {
                        if (it.status != DownloadStatus.IN_PROGRESS) {
                            ivImage.visibility = View.VISIBLE
                        }
                        it
                    }
                    .throttleLast(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ item ->
                        when(item.status) {
                            DownloadStatus.IN_PROGRESS -> {
                                tvProgress.text = String.format(getString(R.string.progress_text), item.progress)
                            }
                            else -> {
                                ivImage.visibility = View.VISIBLE
                            }
                        }
                    }, { t ->
                        t.printStackTrace()
                    })
            }
        }

    }
}

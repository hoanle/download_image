package com.example.downloadimage

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageManager(context: Context) {

    private val cache = hashMapOf<String, Bitmap?>()
    private var tempStorage: File? = null
    private var perStorage: File? = null
    private var downloadListener: PublishSubject<DownloadItem> = PublishSubject.create()

    init {
        //Create a temp folder
        tempStorage = File(context.getExternalFilesDir(context.getString(R.string.temp_folder)).toString())
        val tempStorageExisted = tempStorage?.exists() ?: false
        if (!tempStorageExisted) {
            tempStorage?.mkdir()
        }

        perStorage = File(context.getExternalFilesDir(context.getString(R.string.per_folder)).toString())
        val perStorageExisted = tempStorage?.exists() ?: false
        if (!perStorageExisted) {
            perStorage?.mkdir()
        }
    }

    fun displayImage(downloadUrl: String, view: ImageView): PublishSubject<DownloadItem> {
        if (cache[downloadUrl] != null) {
            view.setImageBitmap(cache[downloadUrl])
            downloadListener.onNext(
                DownloadItem(
                    DownloadStatus.DOWNLOADED,
                    downloadUrl,
                    (cache[downloadUrl]!!.allocationByteCount / 1024).toLong()
                )
            )
        } else {
            try {
                val url = URL(downloadUrl)

                val file = File(perStorage, url.path.replace("/", "_"))
                val fileExisted = file.exists()

                if (fileExisted) {
                    Single.just(true)
                        .subscribeOn(Schedulers.io())
                        .map { FileUtils.decodeImage(file) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ bitmap ->
                            cache[downloadUrl] = bitmap
                            view.setImageBitmap(bitmap)
                            downloadListener.onNext(
                                DownloadItem(
                                    DownloadStatus.DOWNLOADED,
                                    downloadUrl,
                                    (bitmap.allocationByteCount / 1024).toLong()
                                )
                            )
                        }, {
                            it.printStackTrace()
                        })

                } else {
                    createFileDownloadObservable(downloadUrl)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            view.setImageBitmap(it)
                            cache[downloadUrl] = it
                            downloadListener.onNext(
                                DownloadItem(
                                    DownloadStatus.DOWNLOADED,
                                    downloadUrl,
                                    (it.allocationByteCount / 1024).toLong()
                                )
                            )
                        }, {
                            it.printStackTrace()
                        })
                }
            } catch (e: java.lang.Exception) {
                view.setImageResource(R.mipmap.ic_launcher)
                downloadListener.onNext(
                    DownloadItem(
                        DownloadStatus.FAILED,
                        downloadUrl,
                        0
                    )
                )
            }
        }
        return downloadListener
    }

    //Observable to perform the download
    private fun createFileDownloadObservable(downloadUrl: String): Maybe<Bitmap> {
        return Maybe.create {
            var outputFile: File? = null
            val url = URL(downloadUrl)
            val localFileName = url.path.replace("/", "_")
            try {
                val c: HttpURLConnection = url.openConnection() as HttpURLConnection
                c.requestMethod = "GET"
                c.connect()

                if (c.responseCode == HttpURLConnection.HTTP_OK) {
                    outputFile = File(tempStorage, localFileName)
                    val outputFileExisted = outputFile.exists()

                    //Create New File if not present
                    if (!outputFileExisted) {
                        outputFile.createNewFile()
                    }

                    val fileOutputStream = FileOutputStream(outputFile)

                    val inputStream: InputStream = c.inputStream

                    val buffer = ByteArray(512)

                    do {
                        val len = inputStream.read(buffer)
                        if (len == -1) break
                        fileOutputStream.write(buffer, 0, len)
                        downloadListener.onNext(
                            DownloadItem(
                                DownloadStatus.IN_PROGRESS,
                                downloadUrl,
                                fileOutputStream.channel.size() / 1024
                            )
                        )
                    } while (true)

                    fileOutputStream.close()
                    inputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                outputFile = null
            } finally {
                if (outputFile != null) {
                    val desFile = File(perStorage, localFileName)
                    val bitmap = FileUtils.decodeImage(outputFile)
                    FileUtils.copyFile(outputFile, desFile)
                    it.onSuccess(bitmap)
                } else {
                    it.onSuccess(null)
                }
            }
        }
    }
}
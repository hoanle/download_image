package com.example.downloadimage

data class DownloadItem(
    val status: DownloadStatus = DownloadStatus.NOT_STARTED,
    val url: String = "",
    val progress: Long = 0
)
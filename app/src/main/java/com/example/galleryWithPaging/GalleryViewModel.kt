package com.example.galleryWithPaging

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val factory = PixabayDataSourceFactory(application)
    val pagedListLiveData = factory.toLiveData(1)

    val netWorkStatus = Transformations.switchMap(factory.pixabayDataSource) { it.netWorkStatus }

    fun resetQuery() {
        pagedListLiveData.value?.dataSource?.invalidate()
    }

    fun retry() {
        factory.pixabayDataSource.value?.retry?.invoke()
    }
}
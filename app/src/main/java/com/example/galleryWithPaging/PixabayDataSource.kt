package com.example.galleryWithPaging

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

enum class NetWorkStatus {
    INITIAL_LADING,
    LOADING,
    LOADED,
    FAILED,
    COMPLETED
}

class PixabayDataSource(private val context: Context) : PageKeyedDataSource<Int, PhotoItem>() {

    //声明保存数据状态的函数，用来记录
    var retry: (() -> Any)? = null
    private val _netWorkStatus = MutableLiveData<NetWorkStatus>()
    val netWorkStatus: LiveData<NetWorkStatus> get() = _netWorkStatus

    private val queryKey =
        arrayOf("apple", "dog", "car", "beauty", "love", "computer", "flower", "animal").random()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, PhotoItem>
    ) {
        retry = null
        _netWorkStatus.postValue(NetWorkStatus.INITIAL_LADING)
        val url =
            "https://pixabay.com/api/?key=15258738-a05e29c1621e54c726437b8b1&q=${queryKey}&per_page=50&page=1"
        StringRequest(
            Request.Method.GET, url,
            Response.Listener {
                val dataList = Gson().fromJson(it, Pixabay::class.java).hits.toList()
                callback.onResult(dataList, null, 2)
                _netWorkStatus.postValue(NetWorkStatus.LOADED)
            },
            Response.ErrorListener {
                retry = { loadInitial(params, callback) }
                _netWorkStatus.postValue(NetWorkStatus.FAILED)
                Log.d("------ERROR------", "loadMsg:$it")

            }
        ).also { VolleySingleton.getInstance(context).requestQueue.add(it) }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {

        retry = null
        _netWorkStatus.postValue(NetWorkStatus.LOADING)
        val url =
            "https://pixabay.com/api/?key=15258738-a05e29c1621e54c726437b8b1&q=${queryKey}&per_page=50&page=${params.key}"
        StringRequest(
            Request.Method.GET, url,
            Response.Listener {
                val dataList = Gson().fromJson(it, Pixabay::class.java).hits.toList()
                callback.onResult(dataList, params.key + 1)
                _netWorkStatus.postValue(NetWorkStatus.LOADED)

            },
            Response.ErrorListener {
                if (it.toString() == "com.android.volley.ClientError") {
                    _netWorkStatus.postValue(NetWorkStatus.COMPLETED)
                } else {
                    retry = { loadAfter(params, callback) }
                    _netWorkStatus.postValue(NetWorkStatus.FAILED)
                }
                Log.d("------ERROR------", "loadMsg:$it")

            }
        ).also { VolleySingleton.getInstance(context).requestQueue.add(it) }
    }
}

package com.example.networkcamera

import android.util.Log
import com.example.networkcamera.dataType.*
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class APIManager : Observable() {

    companion object {
        val instance : APIManager = APIManager()
    }

    private var client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(LoggingInterceptor()).build()

    //建立攔截器，在HTTP結束後打印response
    private class LoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val t1 = System.nanoTime()
            val response = chain.proceed(request)

            response.body?.let {
                val t2 = System.nanoTime()
                val contentType = it.contentType()
                val content = it.string()
                val url = response.request.url.toString().replace(API_URL, "https:/")
                Log.e(url,"${String.format("%.1f", (t2 - t1) / 1e6)}ms $content")

                val wrappedBody = content.toResponseBody(contentType)
                return response.newBuilder().body(wrappedBody).build()
            }
            return chain.proceed(request)
        }
    }

    private fun httpPost(url: String, req: Any, resOfT: Class<*>? = null, save: Boolean = false){
        val parameter = Gson().toJson(req)
        val urlTag = url.replace(API_URL, "https:/")
        Log.e(urlTag, parameter)

        val body = parameter.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val reqBuilder = Request.Builder().url(url).post(body)

        client.newCall(reqBuilder.build()).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (resOfT == null) return

                try {
                    val json = response.body?.string() as String
                    val res = Gson().fromJson(json, resOfT)

                    notifyChanged(res)
                } catch (e: Exception){
                    Log.e(url, "$e")
                    //notifyChanged(ErrorMsg("$e"))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("onFailure", "$e")
                //notifyChanged(ErrorMsg("$e"))
            }
        })
    }

    private fun notifyChanged(res: Any){
        setChanged()
        notifyObservers(res)
    }


    fun doCameraSet(req: CameraSetReq) = httpPost("${API_URL}/camera/set", req, CameraSetRes::class.java)

    fun doCameraDelete(req: CameraDeleteReq) = httpPost("${API_URL}/camera/delete", req, CameraDeleteRes::class.java)

    fun doCameraList() = httpPost("${API_URL}/camera/list", CameraSetReq(""), CameraListRes::class.java)
}
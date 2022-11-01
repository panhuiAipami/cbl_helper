package com.qyc.cbl_helper.http

import android.util.Log
import com.qyc.cbl_helper.common.PingAnSyncHelper


import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object PingAn4sApiService : BaseApiService() {
    override fun getApiBaseUrl() = "https://4scloud-web.pingan.com/"

    override fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getApiBaseUrl())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getClient())
            .build()
    }

    override fun tokenExpiredInterceptor() = Interceptor { chain ->
        // Token 失效 401 处理
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
//            PushMessageNotificationHelper.showTpAppLogoutNotify(TpAppTypeEnum.HHB)
            PingAnSyncHelper.clearAllInfo(response.code)
        }else if(response.code == 500){
            Log.e("PingAn4sApiService","error:$response")
        }
        response
    }

}
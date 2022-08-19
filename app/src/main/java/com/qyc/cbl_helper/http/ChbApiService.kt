package cn.cpocar.qyc_cbl.http

import com.qyc.cbl_helper.http.BaseApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ChbApiService : BaseApiService() {
    override fun getApiBaseUrl() = "https://thbapp.cpic.com.cn/rn-thb-http/car/"

    override fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getApiBaseUrl())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getClient())
            .build()
    }

}
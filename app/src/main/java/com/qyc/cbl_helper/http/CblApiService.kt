package com.qyc.cbl_helper.http

import android.util.Log
import com.qyc.cbl_helper.common.UserInfoHelper
import com.qyc.cbl_helper.constant.AppConstant
import okhttp3.Interceptor

/**
 * 车百灵 API 服务类
 * User: wanggang@cpocar.cn
 * Date: 2020/10/23 11:12
 */
object CblApiService : BaseApiService() {
    private val mUserInfoHelper: UserInfoHelper = UserInfoHelper

    override fun getApiBaseUrl() = if (AppConstant.DEBUG) "https://test-lark.cpocar.cn/api/" else "https://cbl.qyccar.com/api/"

    override fun headerTokenInterceptor() = Interceptor { chain ->
        // 统一 Header Token 处理
        val originalRequest = chain.request()
        if (!mUserInfoHelper.isLogin()) {
            chain.proceed(originalRequest)
        } else {
            chain.proceed(originalRequest.newBuilder().header("x-lark-token", mUserInfoHelper.getAppToken()!!).build())
        }
    }

    override fun tokenExpiredInterceptor() = Interceptor { chain ->
        // Token 失效 401 处理
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            // 登出
            if (mUserInfoHelper.isLogin()) {
                mUserInfoHelper.logout()
            }
        }else if(response.code == 500){
            Log.e("a","-------response------>$response")
        }
        response
    }

}
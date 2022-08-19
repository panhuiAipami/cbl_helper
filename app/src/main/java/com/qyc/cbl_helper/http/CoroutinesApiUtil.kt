package com.qyc.cbl_helper.http

import com.qyc.cbl_helper.http.exception.ApiResultFailException
import com.qyc.cbl_helper.http.response.base.BaseResponse
import com.qyc.cbl_helper.http.response.base.CommonResponse


/**
 * 协程Api调用统一处理
 * @param sucCall  成功得到数据回调
 * @param failCall 失败时执行回调
 * @param apiCall  api 调用方法
 */
suspend fun <T> coroutineApiCall(sucCall: ((T) -> Unit)? = null, failCall: ((Throwable) -> Unit)? = null, apiCall: suspend () -> CommonResponse<T>): T {
    try {
        val result = apiCall()
        if (result.isSuccess() && null != result.data) {
            val retData = result.data!!
            sucCall?.invoke(retData)
            return retData
        } else {
            val e = ApiResultFailException(result.result, result.message)
            failCall?.invoke(e)
            throw e
        }
    } catch (e: Throwable) {
        failCall?.invoke(e)
        throw e
    }
}

/**
 * 协程Api调用统一处理
 * @param sucCall  成功时回调
 * @param failCall 失败时执行回调
 * @param apiCall   api 调用方法
 */
suspend fun coroutineApiCallBase(sucCall: (() -> Unit)? = null, failCall: ((Throwable) -> Unit)? = null, apiCall: suspend () -> BaseResponse): Boolean {
    try {
        val result = apiCall()
        if (result.isSuccess()) {
            sucCall?.invoke()
            return true
        } else {
            val e = ApiResultFailException(result.result, result.message)
            failCall?.invoke(e)
            throw e
        }
    } catch (e: Throwable) {
        failCall?.invoke(e)
        throw e
    }
}
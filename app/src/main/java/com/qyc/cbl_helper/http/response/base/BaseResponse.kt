package com.qyc.cbl_helper.http.response.base

/**
 * API 接口返回基类
 * User: wanggang@cpocar.cn
 * Date: 2019/3/13 16:29
 */
open class BaseResponse {
    var result: String? = null // 返回结果，"OK"-成功，"FAIL"-失败
    var message: String? = null // 返回消息，如果失败则返回失败原因

    fun isSuccess() = "OK".equals(result, ignoreCase = true)
}
package com.qyc.cbl_helper.http.response.base

/**
 * API 接口公共返回类
 * User: wanggang@cpocar.cn
 * Date: 2019/3/14 14:36
 */
class CommonResponse<T> : BaseResponse() {
    var data: T? = null
}
package com.qyc.cbl_helper.http.exception

/**
 * API 返回失败统一异常
 * User: wanggang@cpocar.cn
 * Date: 2019/3/14 11:00
 */
class ApiResultFailException(val code: String?, message: String?) : Exception(message)
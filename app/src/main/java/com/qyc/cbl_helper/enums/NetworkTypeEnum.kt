package com.qyc.cbl_helper.enums

/**
 * 网络类型枚举
 * User: wanggang@cpocar.cn
 * Date: 2019/10/12 19:29
 */
enum class NetworkTypeEnum(val value: String) {
    /** 无网络 */
    NO_NETWORK("no_network"),

    /** WIFI */
    WIFI("wifi"),

    /** 4G */
    CELLULAR_4G("4g"),

    /** 3G */
    CELLULAR_3G("3g"),

    /** 2G */
    CELLULAR_2G("2g"),

    /** 移动网络（未识别到具体是4G还是3G这些）*/
    CELLULAR("cellular"),

    /** 未知 */
    UNKNOWN("unknown")
}
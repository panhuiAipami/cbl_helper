package com.qyc.cbl_helper.util

import android.os.Build

/**
 * 设备工具类
 * User: wanggang@cpocar.cn
 * Date: 2020/8/29 13:37
 */
class DeviceUtil {

    companion object {
        /**
         * 是否是华为手机（含荣耀）
         */
        fun isHwPhone(): Boolean = Build.MANUFACTURER.equals("HUAWEI", true)

        /**
         * 是否是小米手机（含红米）
         */
        fun isMiPhone() = Build.MANUFACTURER.equals("Xiaomi", true)

        /**
         * 是否是OPPO
         */
        fun isOppo() = Build.MANUFACTURER.equals("oppo", true)

        /**
         * 是否是VIVO
         */
        fun isVivo() = Build.MANUFACTURER.equals("vivo", true)
    }

}
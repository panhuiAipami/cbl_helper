package com.qyc.cbl_helper

import android.app.Application
import android.util.Log

import com.orhanobut.hawk.Hawk
import com.qyc.cbl_helper.constant.AppConstant


/**
 * 应用 Application
 * User: wanggang@cpocar.cn
 * Date: 2020/10/23 13:38
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        init()
    }

    private fun init() {
        Log.i(AppConstant.TAG_COMMON, "AppApplication -> init()")
        Hawk.init(this).build()
    }

    companion object {
        private lateinit var mInstance: MyApplication
        fun getInstance(): MyApplication = mInstance
    }
}
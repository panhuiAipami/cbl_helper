package com.qyc.cbl_helper.common

import android.text.TextUtils
import com.orhanobut.hawk.Hawk

/**
 * 用户信息辅助类
 * User: wanggang@cpocar.cn
 * Date: 2020/10/23 11:32
 */
object UserInfoHelper {
    /** 本地存储Key - APP Token */
    private const val LS_KEY_APP_TOKEN = "ls_key_app_token"

    /** 本地存储Key - uid */
    private const val LS_KEY_UID = "ls_key_uid"

    /** 本地存储Key - 新线索铃声（也是通知渠道名） */
    private const val LS_KEY_NEW_CLUE_NOTIFY_SOUND = "ls_new_clue_notify_sound"

    private var mAppToken: String? = null
    private var mUid: String? = null
    private var mNewClueNotifySound: String? = null

    fun init() {
        if (null == mAppToken) {
            mAppToken = Hawk.get<String>(LS_KEY_APP_TOKEN)
        }
        if (null == mUid) {
            mUid = Hawk.get<String>(LS_KEY_UID)
        }
        if (null == mNewClueNotifySound) {
            mNewClueNotifySound = Hawk.get<String>(LS_KEY_NEW_CLUE_NOTIFY_SOUND)
        }
    }

    fun getAppToken(): String? = mAppToken

    fun getUid(): String? = mUid

    fun getNewClueNotifySound(): String? = mNewClueNotifySound

    fun isLogin(): Boolean = !TextUtils.isEmpty(mAppToken)

    fun setAppToken(appToken: String?) {
        mAppToken = appToken
        if (appToken.isNullOrBlank()) {
            Hawk.delete(LS_KEY_APP_TOKEN)
        } else {
            Hawk.put(LS_KEY_APP_TOKEN, mAppToken)
        }
    }

    fun setUid(uid: String?) {
        mUid = uid
        if (uid.isNullOrBlank()) {
            Hawk.delete(LS_KEY_UID)
        } else {
            Hawk.put(LS_KEY_UID, mUid)
        }
    }

    fun setNewClueNotifySound(newClueNotifySound: String?) {
        mNewClueNotifySound = newClueNotifySound
        if (newClueNotifySound.isNullOrBlank()) {
            Hawk.delete(LS_KEY_NEW_CLUE_NOTIFY_SOUND)
        } else {
            Hawk.put(LS_KEY_NEW_CLUE_NOTIFY_SOUND, mNewClueNotifySound)
        }
    }

    fun logout() {
        setAppToken(null)
        setUid(null)
        setNewClueNotifySound(null)
    }

}
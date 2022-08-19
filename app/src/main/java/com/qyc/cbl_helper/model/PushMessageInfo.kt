package com.qyc.cbl_helper.model

import android.os.Parcel
import android.os.Parcelable


/**
 * 推送消息 和 push_message_po.dart 保持一致
 * User: wanggang@cpocar.cn
 * Date: 2019/9/23 15:39
 */
data class PushMessageInfo(
    val title: String?,
    val body: String?,
    val time: Long?,
    val event: String?, // 事件类型
    val subEvent: String? = null, // 子事件类型  <- 这个在同一种 event 有多种跳转时才需要，不用存库，只是通知栏跳转时需要
//    val type: PushMessageTypeEnum, // 推送消息类型
    val msgId: String?, // 消息ID，唯一标识，为了可以点相应页面，能把对应通知栏消息关闭掉
    var receiveUid: String?,
    val resId: String?, // 对应事件类型的ID, 只有 NATIVE 类型才需要使用到
    val url: String?  // 跳转url, 只有 H5 类型才需要使用到
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(body)
        parcel.writeValue(time)
        parcel.writeString(event)
        parcel.writeString(subEvent)
        parcel.writeString(msgId)
        parcel.writeString(receiveUid)
        parcel.writeString(resId)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PushMessageInfo> {
        override fun createFromParcel(parcel: Parcel): PushMessageInfo {
            return PushMessageInfo(parcel)
        }

        override fun newArray(size: Int): Array<PushMessageInfo?> {
            return arrayOfNulls(size)
        }
    }
}
package com.qyc.cbl_helper.smsreceiver

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import android.util.Log
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.model.SmsInfo
import java.util.regex.Pattern

typealias OnReceiveSmsListener = (SmsInfo) -> Unit

class SmsManager {
    private val mSmsAllUri by lazy { Uri.parse("content://sms/") }
    private val mSmsInboxUri by lazy { Uri.parse("content://sms/inbox") }
    private var mSmsObserver: SmsObserver? = null

    fun startListener(context: Context,handle:Handler, onReceiveSmsListener: OnReceiveSmsListener) {
        stopListener(context)
        mSmsObserver = SmsObserver(context, mSmsInboxUri, onReceiveSmsListener, handle)
        context.contentResolver.registerContentObserver(mSmsAllUri, true, mSmsObserver!!)
    }

    fun stopListener(context: Context) {
        if (null != mSmsObserver) {
            context.contentResolver.unregisterContentObserver(mSmsObserver!!)
            mSmsObserver = null
        }
    }

    fun getSmsList(context: Context, startDate: Long?, maxSize: Int?): List<SmsInfo> {
        val retList = ArrayList<SmsInfo>()
        val cursor: Cursor? = context.applicationContext.contentResolver.query(
                mSmsInboxUri,
                arrayOf("_id", Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE, Telephony.Sms.DATE_SENT, Telephony.Sms.READ),
                startDate?.let { "${Telephony.Sms.DATE} > $it" }, null,
                maxSize?.let { "_id DESC LIMIT $it" } ?: "_id DESC" /* 如果要排序就这样写 _id asc LIMIT 10 */
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex("_id"))
                val smsContent = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
                val address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS))
                val date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
                val dateSentColumnIndex = cursor.getColumnIndex(Telephony.Sms.DATE_SENT)
                val dateSent = if (dateSentColumnIndex >= 0) cursor.getLong(dateSentColumnIndex) else null
                val read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ)) // 是否阅读 0未读， 1已读
                retList.add(SmsInfo(
                        id = id,
                        smsContent = smsContent,
                        address = address,
                        date = date,
                        read = read,
                        sendDate = dateSent
                ))
            }
        }
        cursor?.close()
        return retList
    }
}


class SmsObserver(private val context: Context, private val smsInboxUri: Uri, private val onReceiveSmsListener: OnReceiveSmsListener, handler: Handler) : ContentObserver(handler) {
    private val mHandler // 更新UI线程
            : Handler = handler
    private val uriPattern by lazy { Pattern.compile("(content://sms/)\\d+") }

    // 每当有新短信到来时会回调
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        val uriStr = uri?.toString()
//        SamplingHelper.sampling("SmsSyncService", "sms_callback", "uri" to "$uriStr")
        Log.i(AppConstant.TAG_SMS_RECEIVER, "SmsObserver onChange uri：$uriStr")
        super.onChange(selfChange)
        if (null != uriStr && (uriStr == "content://sms/inbox" || uriStr.startsWith("content://sms/inbox/") || uriPattern.matcher(uriStr).find())) {
            val cursor: Cursor? = context.applicationContext.contentResolver.query(smsInboxUri, arrayOf("_id", Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE, Telephony.Sms.DATE_SENT, Telephony.Sms.READ), null, null, "_id DESC LIMIT 1")
            // 只取一条回调
            if (cursor != null && cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex("_id"))
                val smsContent = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
                val address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS))
                val date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
                val dateSentColumnIndex = cursor.getColumnIndex(Telephony.Sms.DATE_SENT)
                val dateSent = if (dateSentColumnIndex >= 0) cursor.getLong(dateSentColumnIndex) else null
                val read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ)) // 是否阅读 0未读， 1已读

//                val phone = "5511"//平安发短信的号码会变
//                var code = ""
//
//                // 判断手机号是否为目标号码
//                if (address.endsWith(phone) || smsContent.contains("【中国平安】")) {
//
//                    // 正则表达式截取短信中的6位验证码
//                    val pattern: Pattern = Pattern.compile("(\\d{6})")
//                    val matcher: Matcher = pattern.matcher(smsContent)
//
//                    // 如果找到通过Handler发送给主线程
//                    if (matcher.find()) {
//                        code = matcher.group(0)
//                    }
//                }
//
//                mHandler.obtainMessage(0, code).sendToTarget()

                onReceiveSmsListener(SmsInfo(
                        id = id,
                        smsContent = smsContent,
                        address = address,
                        date = date,
                        read = read,
                        sendDate = dateSent
                ))
            }
            cursor?.close()
        }
    }
}
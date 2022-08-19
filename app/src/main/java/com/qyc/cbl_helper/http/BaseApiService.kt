package com.qyc.cbl_helper.http

import android.util.Log
import com.qyc.cbl_helper.constant.AppConstant
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.io.IOException
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


/**
 * API Service 抽象类
 * User: wanggang@cpocar.cn
 * Date: 2019/3/14 11:36
 */
abstract class BaseApiService {
    private val mRetrofit: Retrofit

    init {
        mRetrofit = this.buildRetrofit()
    }

    @Suppress("ConstantConditionIf")
    protected fun getClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.i(TAG, "retrofitBack = $message")
            }
        })
        loggingInterceptor.level = if (AppConstant.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        val (sslSocketFactory, trustManager) = getSocketFactory()
        return OkHttpClient.Builder()
            .addInterceptor(headerTokenInterceptor())
            .addInterceptor(loggingInterceptor)
            .addInterceptor(tokenExpiredInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(readTimeout(), TimeUnit.SECONDS)
            .writeTimeout(writeTimeout(), TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .cookieJar(getCookieJar())
            .proxy(Proxy.NO_PROXY)
            .proxySelector(object : ProxySelector() {
                override fun select(uri: URI?): MutableList<Proxy> = mutableListOf(Proxy.NO_PROXY)
                override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {}

            })
            .build()
    }

    // 这里我们使用host name作为cookie保存的key
    private fun getCookieJar() = object : CookieJar {
        private val cookieStore = HashMap<HttpUrl?, List<Cookie>?>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host.toHttpUrlOrNull()] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookies = cookieStore[url.host.toHttpUrlOrNull()]
            return cookies ?: ArrayList()
        }
    }

    // HTTPS
    private fun getSocketFactory(): Pair<SSLSocketFactory, X509TrustManager> {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            return sslSocketFactory to (trustAllCerts[0] as X509TrustManager)
        } catch (e: Exception) {
            Log.i(TAG, "ssl Exception e = " + e.message)
            throw e
        }
    }

    abstract fun getApiBaseUrl(): String

    // 资源读取超时时间，单位秒， 默认为10s，重写返回可以修改时间
    open fun readTimeout(): Long = 10

    // 资源定入超时时间，单位秒，默认为10s，重写返回可以修改时间
    open fun writeTimeout(): Long = 10

    fun <T> buildApi(apiClass: Class<T>): T {
        return mRetrofit.create(apiClass)
    }

    protected open fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getApiBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getClient())
            .build()
    }

    protected open fun headerTokenInterceptor() = Interceptor { chain -> chain.proceed(chain.request()) }

    protected open fun tokenExpiredInterceptor() = Interceptor { chain -> chain.proceed(chain.request()) }

    companion object {
        private const val TAG = "BaseApiService"
    }

}

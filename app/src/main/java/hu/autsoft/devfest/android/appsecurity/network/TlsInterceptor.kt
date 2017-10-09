package hu.autsoft.devfest.android.appsecurity.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class TlsInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        printTlsAndCipherSuiteInfo(response)
        return response
    }

    private fun printTlsAndCipherSuiteInfo(response: Response?) {
        if (response != null) {
            val handshake = response.handshake()
            if (handshake != null) {
                val cipherSuite = handshake.cipherSuite()
                val tlsVersion = handshake.tlsVersion()
                Log.i("TLS", "Version: $tlsVersion, CipherSuite: $cipherSuite")
            }
        }
    }
}
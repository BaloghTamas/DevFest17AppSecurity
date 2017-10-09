package hu.autsoft.devfest.android.appsecurity.network


import android.content.Context
import android.os.Build
import android.support.annotation.RawRes
import hu.autsoft.devfest.android.appsecurity.R
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import javax.security.cert.CertificateException

private val API_LEVEL_11 = 11
private val API_LEVEL_16 = 16
private val API_LEVEL_20 = 20
private val API_LEVEL_24 = 24

private val KEYSTORE_PASSWORD = "keystorePassword"
private val FORCE_TLS_1_2 = true

object OkHttpProvider {
    fun provideSecureOkHttpClient(context: Context): OkHttpClient {

        val builder = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)

        // For below API 16 use TLS 1.0

        if (FORCE_TLS_1_2) {
            if (Build.VERSION.SDK_INT >= API_LEVEL_16) {
                builder.tlsVersions(TlsVersion.TLS_1_2)
            }
        }

        if (Build.VERSION.SDK_INT >= API_LEVEL_20) {
            builder.tlsVersions(TlsVersion.TLS_1_2)
        }

        //Not secure enough, but compatible - OkHttp Docs: The following ciphers are on the HTTP/2's bad cipher suites list
        if (Build.VERSION.SDK_INT >= API_LEVEL_11) {
            builder.cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA)
        }

        //Secure - Chrome 51 supports
        if (Build.VERSION.SDK_INT >= API_LEVEL_20) {
            builder.cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384)
        }

        //Secure - Chrome 51 supports
        if (Build.VERSION.SDK_INT >= API_LEVEL_24) {
            builder.cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256)
        }


        val client = OkHttpClient.Builder()
                .connectionSpecs(listOf(builder.build()))
                .setSelfSignedCert(context, R.raw.github_cert, KEYSTORE_PASSWORD, FORCE_TLS_1_2)
                .addInterceptor(TlsInterceptor())
                .build()
        return client
    }

}


fun OkHttpClient.Builder.setSelfSignedCert(context: Context, @RawRes bksKeyStore: Int, keystorePassword: String, forceTls12: Boolean): OkHttpClient.Builder {
    try {
        val socketFactory = getPinnedCertSocketFactory(context, bksKeyStore, keystorePassword, forceTls12)
        val trustManager = getPinnedCertTrustManager(context, bksKeyStore, keystorePassword)
        this.sslSocketFactory(socketFactory, trustManager)
    } catch (e: KeyStoreException) {
        e.printStackTrace()
    } catch (e: CertificateException) {
        e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: KeyManagementException) {
        e.printStackTrace()
    }
    return this
}


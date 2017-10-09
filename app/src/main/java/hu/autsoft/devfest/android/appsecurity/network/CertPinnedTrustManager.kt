package hu.autsoft.devfest.android.appsecurity.network

import android.content.Context
import android.os.Build
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.util.*
import javax.net.ssl.*

@Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class, KeyManagementException::class)
fun getPinnedCertSocketFactory(context: Context, keyStoreFile: Int, keystorePassword: String, forceTls12: Boolean): SSLSocketFactory {
    val trustManagers = getTrustManagers(context, keyStoreFile, keystorePassword)
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagers, null)

    var socketFactory = sslContext.socketFactory

    if (forceTls12 && Build.VERSION.SDK_INT in 16..19) {
        socketFactory = ForceTlsSocketFactory(sslContext.socketFactory)
    }
    return socketFactory
}


@Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class, KeyManagementException::class)
fun getPinnedCertTrustManager(context: Context, keyStoreFile: Int, keystorePassword: String): X509TrustManager {
    val trustManagers = getTrustManagers(context, keyStoreFile, keystorePassword)
    return extractX509TrustManager(trustManagers)
}

private fun getTrustManagers(context: Context, keyStoreFile: Int, keystorePassword: String): Array<TrustManager> {
    val trusted = KeyStore.getInstance("BKS")
    val rawResource = context.resources.openRawResource(keyStoreFile)
    trusted.load(rawResource, keystorePassword.toCharArray())

    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(trusted)

    return trustManagerFactory.trustManagers
}


private fun extractX509TrustManager(trustManagers: Array<TrustManager>): X509TrustManager {
    if (trustManagers.size != 1 || !(trustManagers[0] is X509TrustManager)) {
        throw IllegalStateException("Unexpected default trust managers: ${Arrays.toString(trustManagers)}")
    }
    return trustManagers[0] as X509TrustManager
}
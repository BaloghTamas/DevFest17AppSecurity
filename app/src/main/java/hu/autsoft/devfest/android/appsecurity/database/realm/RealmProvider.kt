package hu.autsoft.devfest.android.appsecurity.database.realm

import android.content.Context
import hu.autsoft.key.interator.PasswordManager
import io.realm.Realm
import io.realm.RealmConfiguration


private val DATABASE_NAME = "appsec.realm"

private val AES_KEY = "REALM_AES_256_GCM_KEY"
private val HMAC_KEY = "REALM_HMAC_256_BIT_KEY"

object RealmProvider {

    fun provideRealm(context: Context, password: CharArray?): Realm {
        val config = RealmConfiguration.Builder()
                .name(DATABASE_NAME)
                .encryptionKey(getKeys(context, password))
                .schemaVersion(1)
                .build()
        return Realm.getInstance(config)
    }

    private fun getKeys(context: Context, password: CharArray?): ByteArray {
        val manager = PasswordManager(context)
        if (!manager.isPasswordSet(AES_KEY)) {
            manager.setDerivedPassword(AES_KEY, password!!, 256)
        }
        if (!manager.isPasswordSet(HMAC_KEY)) {
            manager.setDerivedPassword(HMAC_KEY, password!!, 256)
        }

        val aesKey: ByteArray = manager.getDerivedPassword(AES_KEY, password!!, 256)
        val hmacKey: ByteArray = manager.getDerivedPassword(HMAC_KEY, password!!, 256)

        val fullKey: ByteArray = aesKey + hmacKey
        return fullKey
    }


    fun removeKeys(context: Context) {
        val manager = PasswordManager(context)
        manager.removePassword(AES_KEY)
        manager.removePassword(HMAC_KEY)
    }

}
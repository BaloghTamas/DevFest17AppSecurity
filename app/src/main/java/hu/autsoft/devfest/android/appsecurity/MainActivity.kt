package hu.autsoft.devfest.android.appsecurity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import hu.autsoft.devfest.android.appsecurity.database.realm.RealmProvider
import hu.autsoft.devfest.android.appsecurity.database.realm.model.GitHubUser
import hu.autsoft.devfest.android.appsecurity.network.OkHttpProvider
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread {
            val userData = getUserData()
            runOnUiThread {
                saveToRealmAndQuery(userData)
            }

        }.start()

    }

    private fun saveToRealmAndQuery(userData: String?) {
        //Prompt the user for password - never store or burn it in, use it as a char array
        val contentOfAnEditText: CharArray = "SuperSecretUserPassword".toCharArray()

        val realm = RealmProvider.provideRealm(this, contentOfAnEditText)

        val user = GitHubUser()
        user.data = userData
        realm.beginTransaction()
        realm.copyToRealm(user)
        realm.commitTransaction()

        val usersList = realm.where(GitHubUser::class.java).findAll()
        usersList.forEach {
            Log.d("Realm", "GitHub User: ${it.data}.")
            runOnUiThread { textView.text = "${textView.text} GitHub User: ${it.data} \n" }
        }
    }


    @Throws(Exception::class)
    private fun getUserData(): String? {
        val request = Request.Builder()
                .url("https://api.github.com/users/baloghtamas")
                .build()

        val client = OkHttpProvider.provideSecureOkHttpClient(this)

        client.newCall(request).execute().use { response ->
            return response.body()?.string()
        }
    }
}

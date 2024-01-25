package com.example.vkloader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException


class MainActivity : AppCompatActivity() {
    private lateinit var authLauncher: ActivityResultLauncher<Collection<VKScope>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (VK.isLoggedIn()) {
            onLogon()
        } else {
            setupLoginButton()
        }
    }

    private fun setupLoginButton() {
        authLauncher = VK.login(this) { result: VKAuthenticationResult ->
            when (result) {
                is VKAuthenticationResult.Success -> onLogon()
                is VKAuthenticationResult.Failed -> onFailure(result.exception)
            }
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            authLauncher.launch(arrayListOf(VKScope.PHOTOS))
        }
    }

    private fun onLogon() {
        startActivity(Intent(this@MainActivity, GalleryActivity::class.java))
        finish()
    }

    private fun onFailure(exception: VKAuthException) {
        Log.e("login", exception.message.toString())
        showSnackbar(R.string.auth_failed) {
            authLauncher.launch(arrayListOf(VKScope.PHOTOS))
        }
    }

    private fun showSnackbar(messageResId: Int, action: () -> Unit) {
        Snackbar.make(findViewById(R.id.login_button), getString(messageResId), Snackbar.LENGTH_LONG)
            .setAction("retry") { action.invoke() }
            .show()
    }
}
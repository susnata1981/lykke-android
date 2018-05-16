package com.lykke.mobile.ui.login

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.lykke.mobile.R
import com.lykke.mobile.StartActivity
import com.lykke.mobile.ViewModelFactory
import kotlinx.android.synthetic.main.activity_login.loginBtn
import kotlinx.android.synthetic.main.activity_login.progressBar
import java.util.*

class LoginActivity : AppCompatActivity(), LoginViewModel.Navigator {
  companion object {
    private const val TAG = "LoginActivity"
    private const val EMAIL = "email"
  }

  private lateinit var mViewModel: LoginViewModel

  override fun showLoginFailed() {
    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
  }

  override fun openMainActivity() {
    val intent = Intent(this@LoginActivity, StartActivity::class.java)
    startActivity(intent)
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
//    setSupportActionBar(toolbar)

    mViewModel = ViewModelFactory.getInstance(application).create(LoginViewModel::class.java)
    mViewModel.setNavigator(this)

    mViewModel.getUIViewModel().getProgressBarVisibility().observe(this, Observer { visible ->
      if (visible!!) {
        loginBtn.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
      } else {
        loginBtn.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
      }
    })

    mViewModel.getUIViewModel().getStatus().observe(this, Observer {
      status: String? ->
      status?.let {
        Snackbar.make(window.decorView.rootView, status, Snackbar.LENGTH_SHORT).show()
      }
    })

    loginBtn.setReadPermissions(Arrays.asList(EMAIL))

    // Callback registration
    loginBtn.registerCallback(
        mViewModel.getUIViewModel().getCallbackManager(),
        mViewModel.getUIViewModel().getFacebookCallback())
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    Log.d(TAG, "onActivityResult called")
    mViewModel.getUIViewModel().getCallbackManager()
        .onActivityResult(requestCode, resultCode, data)
  }
}

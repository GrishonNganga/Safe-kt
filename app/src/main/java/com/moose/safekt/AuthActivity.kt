package com.moose.safekt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.BatchResultToken
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {
    private lateinit var phoneAuthProvider: PhoneAuthProvider
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var verificationId:String = ""
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        getCallbacks()
        auth = FirebaseAuth.getInstance()
        phoneAuthProvider = PhoneAuthProvider.getInstance()

        btn_signup.setOnClickListener {
            var number = phone.text.toString()
            val code = ccp.selectedCountryCode

            if (number.length < 9){
                layout.error = "Invalid number"
            }
            else{
                number = "+$code$number"
                instruction.text = resources.getString(R.string.code_sent, number)
                progress_bar.visibility = View.VISIBLE
                phone_fields.visibility = View.GONE
                code_fields.visibility = View.VISIBLE
                startAuth(number)
            }
        }

        btn_code.setOnClickListener {
            val phoneCode = code.text.toString()
            val credential = PhoneAuthProvider.getCredential(verificationId, phoneCode)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun getCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@AuthActivity, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationID: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationID, token)
                verificationId = verificationID
            }
        }
    }

    private fun startAuth(number: String) {
        phoneAuthProvider.verifyPhoneNumber(number, 60, TimeUnit.SECONDS, this, callbacks)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progress_bar.visibility = View.GONE
                    Toast.makeText(this,"Sign In success", Toast.LENGTH_SHORT).show()
                }
                else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        progress_bar.visibility = View.GONE
                        Toast.makeText(this,"Invalid code", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}

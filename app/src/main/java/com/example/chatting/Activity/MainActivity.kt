package com.example.chatting.Activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast

import com.example.chatting.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity()
{
    // view binding
    private lateinit var binding : ActivityMainBinding

    // si le code de verification echoue, utliser le renvoie de code

    private var forceResendingToken : PhoneAuthProvider.ForceResendingToken ? = null

    private var mCallBacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks ? = null
    private var mVerificationId : String ? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private val TAG = "MAIN_TAG"

    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneLl.visibility = View.VISIBLE
        binding.codeLl.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            override fun onVerificationCompleted(phoneAuthCredential : PhoneAuthCredential)
            {
                Log.d(TAG,"onVerificationCompleted:")
                signInWithPhoneAuthCredential(phoneAuthCredential)

            }

            override fun onVerificationFailed(e: FirebaseException)
            {

                progressDialog.dismiss()
                Log.d(TAG,"onVerificationFailed: ${e.message}")
                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()

            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken)
            {

                Log.d(TAG, "onCodeSend : $verificationId")
                mVerificationId = verificationId
                forceResendingToken = token
                progressDialog.dismiss()

                //hide phone layout, show code layout
                binding.phoneLl.visibility = View.GONE
                binding.codeLl.visibility = View.VISIBLE
                Toast.makeText(this@MainActivity, "Verification code send .....", Toast.LENGTH_SHORT).show()
                binding.resendCode.text = "Please type the verification code we send to ${binding.edtPhoneNumber.text.toString().trim() }"

            }

        }

        binding.btnPhoneContinue.setOnClickListener {
             //input phone number
            val phone = binding.edtPhoneNumber.text.toString().trim()

            // validate phone number

            if (TextUtils.isEmpty(phone))
            {
                Toast.makeText(this@MainActivity, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }
            else
            {
                startPhoneNumberVerification(phone)
            }

            // Start new activity
            val intent = Intent(this, OtpActivity :: class.java)
            startActivity(intent)
        }

        binding.resendCode.setOnClickListener{
            val phone = binding.edtPhoneNumber.text.toString().trim()

            if (TextUtils.isEmpty(phone))
            {
                Toast.makeText(this@MainActivity, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }
            else
            {
                resendVerificationCode(phone , forceResendingToken)
            }
        }

        binding.btnSubmitCode.setOnClickListener{
            val code = binding.edtCode.text.toString().trim()
            var id = ""
            if (TextUtils.isEmpty(code))
            {
                Toast.makeText(this@MainActivity, "Please enter verification code", Toast.LENGTH_SHORT).show()
            }
            else
            {
                verifyPhoneNumberWithCode(mVerificationId ,code)
                val intent = Intent(this, OtpActivity :: class.java)
                startActivity(intent)
            }
        }


    }



    private fun startPhoneNumberVerification (phone : String)
    {
        Log.d(TAG, "startPhoneNumberVerification : $phone")
        progressDialog.setMessage("Verifying Phone Number.......")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber (phone)
            .setTimeout (60L, TimeUnit.SECONDS)
            .setActivity (this)
            .setCallbacks(mCallBacks!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode (phone : String, token: PhoneAuthProvider.ForceResendingToken?)
    {
        progressDialog.setMessage("Resending code.......")
        progressDialog.show()

       Log.d(TAG, "resendVerificationCode : $phone")

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber (phone)
            .setTimeout (60L, TimeUnit.SECONDS)
            .setActivity (this)
            .setCallbacks(mCallBacks!!)
            .setForceResendingToken(token!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode (verificationId : String?, code : String)
    {
        Log.d(TAG, "verifyPhoneNumberWithCode : $verificationId $code ")
        progressDialog.setMessage("Verifying code .......")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId.toString(), code)
        signInWithPhoneAuthCredential (credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        Log.d(TAG, "signInWithPhoneAuthCredential : ")

        progressDialog.setMessage("Logging In ......")

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val phone = firebaseAuth.currentUser?.phoneNumber
                Toast.makeText(this, "Logged In as $phone", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, OtpActivity :: class.java))
                finish()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()

            }

    }
}

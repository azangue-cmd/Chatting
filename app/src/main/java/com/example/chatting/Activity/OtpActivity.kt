package com.example.chatting.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatting.databinding.ActivityOtpBinding
import com.google.firebase.auth.FirebaseAuth

class OtpActivity : AppCompatActivity()
{

   private lateinit var firebaseAuth: FirebaseAuth

   // view binding
   private lateinit var binding: ActivityOtpBinding


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // logoutBtn click, Logout the user
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }


    }

    private fun checkUser()
    {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null)
        {
            //logget out
            startActivity(Intent(this, MainActivity :: class.java))
            finish()
        }
        else
        {
            // logged in, get phone number of user
            val phone = firebaseUser.phoneNumber
            // set phone number
            binding.phonetv.text = phone
        }
    }


}

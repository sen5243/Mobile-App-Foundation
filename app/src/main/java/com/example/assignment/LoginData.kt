package com.example.assignment

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

// retrieve login data
class LoginData : AppCompatActivity() {
    lateinit var edtEmail : EditText
    lateinit var edtPassword : EditText
    lateinit var myRef : DatabaseReference
    lateinit var data : ValueEventListener
    lateinit var btnLogin : Button
    lateinit var txtForgotPassword : TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        myRef = FirebaseDatabase.getInstance().reference
        btnLogin = findViewById(R.id.btnLogin)
        auth = Firebase.auth
        txtForgotPassword = findViewById(R.id.txtForgotPassword)

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            signIn(email, password)
        }

        txtForgotPassword.setOnClickListener{
            val email = edtEmail.text.toString()
            password(email)
        }


    }

    private fun signIn(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {//  if sign in success
                        goDashboard(auth.uid.toString())//passes user uid to dashboard function
                    }
                }
            auth.signInWithEmailAndPassword(email, password).addOnFailureListener {task -> // if sign in failed catch exceptions and display error message
                if (task is FirebaseAuthInvalidUserException){
                    Toast.makeText(applicationContext,"Email does not exist",Toast.LENGTH_SHORT).show()
                }
                if (task is FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(applicationContext,"Password Incorrect",Toast.LENGTH_SHORT).show()
                }
                if (task is FirebaseTooManyRequestsException){
                    Toast.makeText(applicationContext,"You've attempted to login too many times, Please try again later",Toast.LENGTH_SHORT).show()
                }
            }
        }
        catch (exception:java.lang.IllegalArgumentException){ //catch exception if the email or password input is missing
            Toast.makeText(this,"Please fill all the field",Toast.LENGTH_SHORT).show()
        }

    }


    // ### Field validation ###
    private fun getData() : Boolean{
        var check = true // initialize the check boolean for validation purpose

        when(check){ // check whether the user has filled up all the fields
            edtEmail.text.isEmpty() -> check = false
            edtPassword.text.isEmpty() -> check = false
            else -> {}
        }

        if(!check){ // send Toast message to user if user not filled up all of the fields
            Toast.makeText(this,"Please Enter All the Information",Toast.LENGTH_SHORT).show()
        }
        return check //return true if all fields are filled up , otherwise return false
    }

    // ### Dashboard function ###
    private fun goDashboard(uid:String) { //function go to dashboard page
        val intent = Intent(this, Dashboard::class.java) //activate this part when linked to dashboard page
        intent.putExtra("UID",uid) //passes uid string to next page
        if (getData()){ // go to next page if user has filled up all of the fields
            startActivity(intent)
        }

    }
    // ### SignUp function ###
    fun goSignUp(view: View) { // go to signUp page if user haven't registered an account
        val intent = Intent(this, SignupData::class.java) //activate this part when user clicks sign up link
        startActivity(intent)
    }

    private fun password(email: String){ //forgot password function
        try {
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext,"A Password Reset Email Has Been Sent",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(applicationContext,"Failed To Send Password Reset Email, Please Make Sure Email That You Have Entered Is The Correct Email.",Toast.LENGTH_SHORT).show()
                    }

                }
        }
        catch (exception : IllegalArgumentException){ //catch exception if the email input is missing
            Toast.makeText(this,"Please fill in your email",Toast.LENGTH_SHORT).show()
        }
    }
}








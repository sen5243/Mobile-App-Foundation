package com.example.assignment

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.google.firebase.auth.ktx.auth

// retrieve signup data
class SignupData : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var option : Spinner
    lateinit var edtName : EditText
    lateinit var edtAge : EditText
    lateinit var edtWeight : EditText
    lateinit var edtHeight : EditText
    lateinit var edtEmail: EditText
    lateinit var edtPassword : EditText
    lateinit var edtConfirmPassword : EditText
    lateinit var btnSignUp : Button
    lateinit var myRef : DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        option = findViewById(R.id.edtGender)
        btnSignUp = findViewById(R.id.btnSignUp)
        myRef = FirebaseDatabase.getInstance().reference
        edtWeight = findViewById(R.id.edtWeight)
        edtHeight = findViewById(R.id.edtHeight)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        edtName = findViewById(R.id.edtName) //the <EditText>, is same to 'as EditText' at the back
        edtAge = findViewById(R.id.edtAge)
        auth = Firebase.auth //firebase authentication, used to store user email and password

        btnSignUp.setOnClickListener {// as the user clicks signup button, the latest entered (error-free) data will then be collected
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            if (getData()){
                createAccount(email, password)
            }

        }

        spinner()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            reload();
        }
    }

    private fun reload(){
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Failed to reload user.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun createAccount(email: String, password: String) { // function to create account
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) { //if sign in successful, redirect user to dashboard
                        dataSave()
                        Toast.makeText(this,"Account has been created",Toast.LENGTH_SHORT).show()
                        dashboard(auth.uid.toString())
                    }
                }
            auth.createUserWithEmailAndPassword(email, password).addOnFailureListener {task ->
                if (task is FirebaseAuthUserCollisionException){ //if fail to sign in, notify user about the error
                    Toast.makeText(this,"This Email has been used",Toast.LENGTH_SHORT).show()
                }
                if (password.length < 6){
                    Toast.makeText(this,"Password must be at least 6 characters long",Toast.LENGTH_SHORT).show()
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(this,"Invalid Email Format",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun dashboard(uid:String){
        val intent = Intent(this, Dashboard::class.java) //activate this part when linked to dashboard page
        intent.putExtra("UID",uid)//passes uid to intent
        Toast.makeText(this,"Your Data Has Been Saved",Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }
    fun dataSave(){ //function to save data
        myRef = FirebaseDatabase.getInstance().reference
        val name = edtName.text.toString()
        val age = edtAge.text.toString()
        val weight = edtWeight.text.toString()
        val height = edtHeight.text.toString()
        val password = edtPassword.text.toString().trim()
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)).toString()
        val time = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)).toString()
        val gender = option.selectedItem.toString()
        val email = edtEmail.text.toString()

        myRef.child(auth.uid.toString()).child("NAME").setValue(name)
        myRef.child(auth.uid.toString()).child("AGE").setValue(age)
        myRef.child(auth.uid.toString()).child("PASSWORD").setValue(password)
        myRef.child(auth.uid.toString()).child("DATE").setValue(date)
        myRef.child(auth.uid.toString()).child("WEIGHT").setValue(weight)
        myRef.child(auth.uid.toString()).child("HEIGHT").setValue(height)
        myRef.child(auth.uid.toString()).child("CALORIES").setValue(0.0)
        myRef.child(auth.uid.toString()).child("BMI").setValue(0.0)
        myRef.child(auth.uid.toString()).child("GENDER").setValue(gender)
        myRef.child(auth.uid.toString()).child("DATE_BMI").setValue("$date\t\t$time")
        myRef.child(auth.uid.toString()).child("DATE_CALORIES").setValue("$date\t\t$time")
        myRef.child(auth.uid.toString()).child("DATE_BURNT").setValue("$date\t\t$time")
        myRef.child(auth.uid.toString()).child("CALORIES_BURNT").setValue(0.0)
        myRef.child(auth.uid.toString()).child("EMAIL").setValue(email)

    }
    private fun getData() : Boolean { //function for data validation

        var check = true

        var errorMessage = ""

        when(check){
            edtName.text.isEmpty() -> check = false
            edtAge.text.isEmpty() -> check = false
            edtWeight.text.isEmpty() -> check = false
            (option.selectedItem == false) -> check = false
            edtHeight.text.isEmpty() -> check = false
            edtEmail.text.isEmpty() -> check = false
            edtPassword.text.isEmpty() -> check = false
            edtConfirmPassword.text.isEmpty() -> check = false
            else -> {}
        }
        if (!check){
            errorMessage += "Please Enter All the Information"
        }
        // checking whether both passwords matches
        else if (edtPassword.text.toString() != edtConfirmPassword.text.toString()) {
            errorMessage += "Password does not match. Please try again."
            check = false
        }

        Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show()

        return check
    }

    private fun spinner() { //spinner function
        val gender = R.array.Gender

        val genderAdapter = ArrayAdapter.createFromResource(this,gender,R.layout.support_simple_spinner_dropdown_item)
        genderAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)


        option.adapter = genderAdapter

        option.onItemSelectedListener = this

    } // array adaptor connects the spinner to the data provided in the array
    override fun onItemSelected (parent: AdapterView<*>?, view: View?, position: Int, id: Long) { // if an option has been selected ...
    }

    override fun onNothingSelected(parent: AdapterView<*>?) { // if an option hasn't selected ...
        val toast = Toast.makeText(applicationContext, "Please choose an option", Toast.LENGTH_SHORT)
        toast.show()
    }
}







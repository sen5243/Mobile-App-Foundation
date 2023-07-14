package com.example.assignment

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class Dashboard : AppCompatActivity() {
    lateinit var txtName : TextView
    lateinit var txtBmiValue : TextView
    lateinit var txtCaloriesValue: TextView
    lateinit var btnNext : Button
    lateinit var txtDate : TextView
    lateinit var myRef : DatabaseReference
    lateinit var data : ValueEventListener
    lateinit var txtBurnt : TextView
    lateinit var txtDateBmi : TextView
    lateinit var txtDateCalories : TextView
    lateinit var txtDateBurnt : TextView
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)
        btnNext = findViewById(R.id.btnNext)
        txtDate = findViewById(R.id.txtDate)
        myRef = FirebaseDatabase.getInstance().reference
        txtName = findViewById(R.id.txtName)
        txtBmiValue = findViewById(R.id.txtBmiValue)
        txtCaloriesValue = findViewById(R.id.txtCaloriesValue)
        txtBurnt = findViewById(R.id.txtBurntValue)
        txtDateBmi = findViewById(R.id.txtDateBmi)
        txtDateCalories = findViewById(R.id.txtDateCalories)
        txtDateBurnt = findViewById(R.id.txtDateBurnt)
        auth = Firebase.auth

        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        txtDate.text = date

        val intent = intent
        val uid = intent.getStringExtra("UID").toString() //get uid string from intent

        if(intent != null){
            data = object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uid = intent.getStringExtra("UID").toString() //get uid string from intent for different scope

                    for (item in snapshot.children){
                        if (item.key == uid){ //displays data based on the given uid
                            txtName.text = item.child("NAME").value.toString()
                            txtBmiValue.text = item.child("BMI").value.toString()
                            txtCaloriesValue.text = String.format("%s\t\tkcal",item.child("CALORIES").value.toString())
                            txtBurnt.text = String.format("%s\t\tkcal",item.child("CALORIES_BURNT").value.toString())
                            txtDateBmi.text = item.child("DATE_BMI").value.toString()
                            txtDateCalories.text = item.child("DATE_CALORIES").value.toString()
                            txtDateBurnt.text = item.child("DATE_BURNT").value.toString()


                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext,"Database Not Found",Toast.LENGTH_SHORT).show()
                }

            }
            myRef.addValueEventListener(data)
            myRef.addListenerForSingleValueEvent(data)
        }

        btnNext.setOnClickListener {
            next(uid) //passes uid string to next() function while button is clicked
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){ //refresh user data if user exist
            reload();
        }
    }

    private fun reload(){ //function to refresh user data
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Failed to reload user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun next(uid:String){ // go to option page with passed email string
        val intent = Intent(this, Option::class.java)
        intent.putExtra("UID",uid)
        startActivity(intent)
    }

    fun logout(view: View){ // back to login page
        auth.signOut()
        val intent = Intent(this,LoginData::class.java)
        startActivity(intent)
    }
}
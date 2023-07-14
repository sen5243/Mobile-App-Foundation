package com.example.assignment

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.lang.NumberFormatException
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class BmiCalculator : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var gender : Spinner
    private lateinit var bmiResult : TextView
    private lateinit var bmiQuality : TextView
    private lateinit var ediAge : EditText
    private lateinit var ediWeight : EditText
    private lateinit var ediHeight : EditText
    private lateinit var btnCal : Button
    private lateinit var myRef : DatabaseReference
    private lateinit var data : ValueEventListener
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmicalculator)

        gender = findViewById(R.id.Gender)
        bmiResult = findViewById(R.id.bmiResult)
        bmiQuality = findViewById(R.id.bmiQuality)
        ediAge = findViewById(R.id.ediAge)
        ediWeight = findViewById(R.id.ediWeight)
        ediHeight = findViewById(R.id.ediHeight)
        btnCal = findViewById(R.id.btnCal)
        myRef = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth //firebase authentication, used to store user email and password

        val uid = intent.getStringExtra("UID").toString()
        val intent = intent

        if(intent != null){

            data = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uid = intent.getStringExtra("UID").toString() // get uid string from the previous page

                    for (item in snapshot.children){
                        if (item.key == uid){
                            val meterHeight = item.child("HEIGHT").value.toString().toDouble()/100
                            ediHeight.setText(meterHeight.toString()) //setting values to the fields with the data based on the uid passed that stored in the Database
                            ediAge.setText(item.child("AGE").value.toString())
                            ediWeight.setText(item.child("WEIGHT").value.toString())
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

        spinner()
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){ //refresh user data if user exist
            reload()
        }
    }

    private fun reload(){ //refresh current user's data
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Failed to reload user data.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun spinner() {
        val genderArray = R.array.Gender

        val genderAdapter = ArrayAdapter.createFromResource(this, genderArray, R.layout.support_simple_spinner_dropdown_item)
        genderAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        gender.adapter = genderAdapter

        gender.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.getItemAtPosition(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
            Toast.makeText(applicationContext, "Please choose an option", Toast.LENGTH_SHORT).show()
    }

    fun dataSave(bmi:Double){ // function to save data
        val uid = intent.getStringExtra("UID").toString() // get uid string from passes intent
        val currentTime = LocalDateTime.now() //get current local date and time based on your phone
        val date = currentTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) //format current date to mm dd, yyyy format
        val time = currentTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)) //format current time to hh:mm:ss AM/PM format
        val genderText = gender.selectedItem.toString() // find current checked radio button id and convert its text to string
        val height = ediHeight.text.toString().toDouble() * 100

        myRef.child(uid).child("DATE_BMI").setValue("$date\t\t$time") // stored current date and time to formatted string
        myRef.child(uid).child("WEIGHT").setValue(ediWeight.text.toString()) //stores user weight
        myRef.child(uid).child("HEIGHT").setValue(height) //stores user height
        myRef.child(uid).child("GENDER").setValue(genderText) //stores user gender
        myRef.child(uid).child("AGE").setValue(ediAge.text.toString()) //stores user age
        myRef.child(uid).child("BMI").setValue(String.format("%.2f",bmi)) //stores user bmi value

    }

    fun bmi(view:View){ //function to display bmi
        try {
            var message = ""

            if (ediAge.text.isNotEmpty()){

                val bmi = bmiCalculate()
                bmiResult.text = String.format("%.2f",bmi)

                val quality = bmiRes(bmi)
                bmiQuality.text = String.format("You are %s",quality)

                dataSave(bmi)
            }
            else{ //if user does not enter the fields
                message += "Please Enter Your Age\n"

                if (ediWeight.text.isEmpty()){
                    message += "Please Enter Your Weight\n"
                }
                if (ediHeight.text.isEmpty()){
                    message += "Please Enter Your Height\n"
                }

                Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
            }

        }
        catch (exception:NumberFormatException){ //catch empty strings
            var message = ""

            if (ediWeight.text.isEmpty()){
                message += "Please Enter Your Weight\n"
            }
            if (ediHeight.text.isEmpty()){
                message += "Please Enter Your Height\n"
            }

            if(ediAge.text.isEmpty()){
                message += "Please Enter Your Age"
            }
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }
    }
    private fun bmiRes(bmi: Double): String { //return bmi status
        val age: Double = ediAge.text.toString().toDouble()
        var ans = ""
        if (age >= 18 ) {
            if (bmi < 16) {
                ans = "Severe Thinness"
            } else if (bmi >= 16 && bmi < 17) {
                ans = "Moderate Thinness"
            } else if (bmi >= 17 && bmi < 18.5) {
                ans = "Mild Thinness"
            } else if (bmi >= 18.5 && bmi < 25) {
                ans = "Normal"
            } else if (bmi >= 25 && bmi < 30) {
                ans = "Overweight"
            } else if (bmi >= 30 && bmi < 35) {
                ans = "Obese Class I"
            } else if (bmi >= 35 && bmi < 40) {
                ans = "Obese Class II"
            } else if (bmi >= 40) {
                ans = "Obese Class III"
            }
        }

        else if (age >= 14 && age < 18) {
            if (bmi < 17){
                ans = "Under Weight"
            } else if(bmi >= 17 && bmi < 24){
                ans = "Healthy"
            } else if(bmi >= 24 && bmi < 28.5){
                ans = "Overweight"
            } else if(bmi >= 28.5){
                ans = "Obese"
            }
        }
        else if (age >= 10 && age < 13) {
            if (bmi < 15){
                ans = "Under Weight"
            } else if(bmi >= 15 && bmi < 21){
                ans = "Healthy"
            } else if(bmi >= 21 && bmi < 24.5){
                ans = "Overweight"
            } else if(bmi >= 24.5){
                ans = "Obese"
            }
        }
        else if (age >= 7 && age <9) {
            if (bmi < 14){
                ans = "Under Weight"
            } else if(bmi >= 14 && bmi < 18){
                ans = "Healthy"
            } else if(bmi >= 18 && bmi < 21){
                ans = "Overweight"
            } else if(bmi >= 21){
                ans = "Obese"
            }
        }
        else if (age < 7) {
            if (bmi < 14){
                ans = "Under Weight"
            } else if(bmi >= 14 && bmi < 17){
                ans = "Healthy"
            } else if(bmi >= 17 && bmi < 18){
                ans = "Overweight"
            } else if(bmi >= 18){
                ans = "Obese"
            }
        }
        return ans
    }

    private fun bmiCalculate(): Double { //calculate bmi value
        val weight = ediWeight.text.toString().toDouble()
        val height = ediHeight.text.toString().toDouble()
        return weight / (height * height)

    }

    fun reset(view: View) { //reset all the input

        val linear = findViewById<ViewGroup>(R.id.linear) // find the top vertical linear layout that contains all of the children
        for (i in 0..linear.childCount) { // loops all the children in the layout
            val childViewGroup = linear.getChildAt(i) //get child id based on the index
            if (childViewGroup is ViewGroup) { //identify whether the child is a part of the ViewGroups
                filter(childViewGroup) //filter the children inside the child if it is a part of the ViewGroups
            }
        }
        /* set the specified fields and values to empty*/
        bmiResult.text = "0"
    }

    private fun filter(viewGroup: ViewGroup) { //to filter the childViews which also a part of the ViewGroups
        for (i in 0..viewGroup.childCount) {
            val childView = viewGroup.getChildAt(i) // get children inside the ViewGroups
            if (childView is ViewGroup) { // recursive function used to further filter the filtered childViews which is also a part of the ViewGroups till it's completely filtered
                filter(childView)
            }

            else{ // clear the input
                if (childView is EditText) {
                    childView.text = null

                    }
                }
            }
        }
    }

package com.example.assignment

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.lang.NumberFormatException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CaloriesNeed : AppCompatActivity() {
    lateinit var grpGender : RadioGroup
    lateinit var grpActivity: RadioGroup
    lateinit var seekAge: SeekBar
    lateinit var txtResult: TextView
    lateinit var txtAgeValue: TextView
    lateinit var edtWeightValue : EditText
    lateinit var edtHeightValue : EditText
    lateinit var myRef : DatabaseReference
    lateinit var data : ValueEventListener
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calories_need)

        seekAge = findViewById(R.id.seekAge)
        txtAgeValue = findViewById(R.id.txtAgeValue)
        grpActivity = findViewById(R.id.grpActivity)
        txtResult = findViewById(R.id.txtResultCalories)
        edtWeightValue = findViewById(R.id.edtWeightValue)
        edtHeightValue = findViewById(R.id.edtHeightValue)
        grpGender = findViewById(R.id.grpGender2)
        myRef = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth //firebase authentication, used to store user email and password

        val intent = intent

        if(intent != null){ // check whether the data has been passed

            data = object :ValueEventListener{ //read data from database
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uid = intent.getStringExtra("UID").toString() // get the passed uid string from the intent

                    for (item in snapshot.children){
                        if (item.key == uid){ // fills up all the inputs with the data stored in the Database based on the given user uid
                            seekAge.progress = item.child("AGE").value.toString().toInt()
                            txtAgeValue.text = item.child("AGE").value.toString()
                            edtWeightValue.setText(item.child("WEIGHT").value.toString())
                            edtHeightValue.setText(item.child("HEIGHT").value.toString())

                            /*check the radiobutton based on user's gender*/

                            if (item.child("GENDER").value.toString() == "Male"){
                                grpGender.check(R.id.rtnMaleCalories)
                            }
                            else if (item.child("GENDER").value.toString() == "Female"){
                                grpGender.check(R.id.rtnFemaleCalories)
                            }

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
    }

    override fun onStart() {
        super.onStart()
        seek() //seekbar function

        val currentUser = auth.currentUser
        if(currentUser != null){ //refresh user data if user exists
            reload();
        }
  }
    private fun reload(){ //function to refresh user data
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful) { //if fail to reload
                Toast.makeText(this, "Failed to reload user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun seek(){ // for the seekbar usage

        seekAge.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) { //while user dragging the seekbar
                txtAgeValue.text = progress.toString() //set seekbar to Textview progress while dragging
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {//while user start dragging the seekbar
                txtAgeValue.text = seekBar.progress.toString() //set seekbar to Textview progress while dragging while user start dragging the seekbar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {//while user stop dragging the seekbar
                txtAgeValue.text = seekBar.progress.toString() //set seekbar progress to Textview while stop dragging
            }
        })
    }

    fun dataSave(result: Double){ //function to save data

        val age = seekAge.progress.toString()
        val weight = edtWeightValue.text.toString()
        val height = edtHeightValue.text.toString()
        val uid = intent.getStringExtra("UID").toString()
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)).toString()
        val time = currentDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)).toString()
        val gender = findViewById<RadioButton>(grpGender.checkedRadioButtonId).text.toString()
        val calories = String.format("%.2f",result)

        myRef.child(uid).child("AGE").setValue(age)
        myRef.child(uid).child("WEIGHT").setValue(weight)
        myRef.child(uid).child("HEIGHT").setValue(height)
        myRef.child(uid).child("CALORIES").setValue(calories)
        myRef.child(uid).child("GENDER").setValue(gender)
        myRef.child(uid).child("DATE_CALORIES").setValue("$date\t\t$time")
    }
    fun calories(view:View){ // function to calculate calories
        var message = ""
        try {
            val bmr = bmrCalculate() //get bmr value from bmrCalculate function
            var multiplier = 0.0 //initializes multiplier
            when(grpActivity.checkedRadioButtonId){ //change multiplier value based on selections of the radio buttons
                R.id.rtnSedentary -> multiplier = 1.2
                R.id.rtnLight -> multiplier = 1.375
                R.id.rtnModerate -> multiplier = 1.55
                R.id.rtnActive -> multiplier = 1.725
                R.id.rtnVery -> multiplier = 1.9
            }
            if (grpActivity.checkedRadioButtonId == -1){ //add message if the activity radio button is not checked
                message +="Please Select Your Activity\n"
            }

            if (grpGender.checkedRadioButtonId == -1){ //add message if the gender radio button is not checked
                message +="Please Select Your Gender\n"
            }

            else if(message == ""){ // calculate and save data if there are no errors
                val result = bmr * multiplier
                txtResult.text = String.format("%.2f kcal",result)
                dataSave(result) // save user data with passed result
                Toast.makeText(this,"Data has been saved",Toast.LENGTH_SHORT).show() //notify user that the data has been saved
            }
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }
        catch (exception:NumberFormatException){ //to catch empty string in input

            if (edtWeightValue.text.isEmpty()){
                message += "Please Enter Your Weight\n"
            }
            if (edtHeightValue.text.isEmpty()){
                message += "Please Enter Your Height\n"
            }

            if (grpActivity.checkedRadioButtonId == -1){ //add message if the activity radio button is not checked
                message +="Please Select Your Activity\n"
            }

            if (grpGender.checkedRadioButtonId == -1){ //add message if the gender radio button is not checked
                message +="Please Select Your Gender\n"
            }

            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }

    }

    private fun bmrCalculate(): Double { //return bmr value

        val age : Int = seekAge.progress
        val weightString : String = edtWeightValue.text.toString()
        val weightValue : Double = weightString.toDouble()
        val heightString : String = edtHeightValue.text.toString()
        val heightValue : Double = heightString.toDouble()
        var bmr = 0.0


        if (grpGender.checkedRadioButtonId == R.id.rtnMaleCalories){
            val constant = 66.47
            val weightMultiplier = 13.75
            val heightMultiplier = 5.003
            val ageMultiplier = 6.755

            bmr = constant + (weightMultiplier * weightValue) + (heightMultiplier * heightValue) - (ageMultiplier * age)
        }

        else if (grpGender.checkedRadioButtonId == R.id.rtnFemaleCalories){
            val constant = 665.1
            val weightMultiplier = 9.563
            val heightMultiplier = 1.850
            val ageMultiplier = 4.676

            bmr = constant + (weightMultiplier * weightValue) + (heightMultiplier * heightValue) - (ageMultiplier * age)
        }
        return bmr // return bmr value for calculation
    }

    fun reset(view: View) { //reset all the input

        val linear = findViewById<ViewGroup>(R.id.vertical)
        for (i in 0..linear.childCount) { //loops through all the children of linear layout
            val childViewGroup = linear.getChildAt(i)
            if (childViewGroup is ViewGroup) { // to filter out the children which are also a ViewGroup
                filter(childViewGroup)
            }
        }
        txtResult.text = null
    }

    private fun filter(viewGroup: ViewGroup) { //to filter the childViews which also a part of the ViewGroups
        for (i in 0..viewGroup.childCount) {
            val childView = viewGroup.getChildAt(i) // get child based on index
            if (childView is ViewGroup) { // recursive function used to further filter the filtered childView till it's completely filtered
                filter(childView)
            }
            if(childView is RadioGroup){ // clear check if the childView is a radioGroup
                childView.clearCheck()
            }
            else{ // clear the inputs
                when (childView) {
                    is EditText -> childView.text = null
                    is SeekBar -> childView.progress = 0
                }
            }
        }
    }

}
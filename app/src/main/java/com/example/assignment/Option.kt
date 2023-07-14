package com.example.assignment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button

class Option : Activity() {

    lateinit var btnCalBMI : Button
    lateinit var btnCalCalories : Button
    lateinit var btnCalCaloriesBurn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.option)

        btnCalBMI = findViewById(R.id.btnCalBMI)
        btnCalCalories = findViewById(R.id.btnCalCalories)
        btnCalCaloriesBurn = findViewById(R.id.btnCalCaloriesBurn)

        val intent = intent

        if (intent != null ){ //check whether the data has been passed
            val uid = intent.getStringExtra("UID").toString() // get user uid string

            btnCalBMI.setOnClickListener { //passing uid to corresponding buttons
                goBMI(uid)
            }

            btnCalCalories.setOnClickListener {
                goCalories(uid)
            }

            btnCalCaloriesBurn.setOnClickListener {
                goCaloriesBurnt(uid)
            }
        }
    }

    private fun goBMI(uid:String) { //activate this part when BMI page linked (can rename func name too)
        val intent = Intent(this, BmiCalculator::class.java) //links to BMI page
        intent.putExtra("UID",uid) //passes uid string to next page
        startActivity(intent) //start activity with passed data
    }

    private fun goCalories(uid:String) { //activate this part when Calories needed page linked (can rename func name too)
        val intent = Intent(this, CaloriesNeed::class.java) //activate this part when linked to calories page
        intent.putExtra("UID",uid)//passes uid string to next page
        startActivity(intent)
    }

    private fun goCaloriesBurnt(uid: String) { //activate this part when Calories Burnt page linked (can rename func name too)
        val intent = Intent(this, CaloriesBurnt::class.java) //activate this part when linked to BMI page
        intent.putExtra("UID",uid)//passes uid string to next page
        startActivity(intent)
    }
}
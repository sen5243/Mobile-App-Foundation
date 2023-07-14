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

class CaloriesBurnt : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var activitySpinner: Spinner
    lateinit var typeSpinner: Spinner
    lateinit var grpGender : RadioGroup
    lateinit var txtMulti : TextView
    lateinit var txtCaloriesBurntValue : TextView
    lateinit var edtWeightValue : EditText
    lateinit var myRef : DatabaseReference
    lateinit var data : ValueEventListener
    lateinit var auth : FirebaseAuth

    var cal = 0.0 // accumulate calories values as the user press the add item button
    var message = "Activities done : \n" // message shown at the textview as user adding items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calories_burnt)
    }

    override fun onStart() {
        super.onStart()
        activitySpinner = findViewById(R.id.spinner_activity)
        typeSpinner = findViewById(R.id.spinner_type)
        grpGender = findViewById(R.id.grpGender2)
        txtMulti = findViewById(R.id.txtActivitiesBurnt)
        edtWeightValue = findViewById(R.id.edtWeightValueBurnt)
        txtCaloriesBurntValue = findViewById(R.id.txtCaloriesBurntValue)
        myRef = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth //firebase authentication, used to store user email and password

        val intent = intent

        val currentUser = auth.currentUser
        if(currentUser != null){ //refresh user data if user exist
            reload();
        }

        if(intent != null){

            data = object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uid = intent.getStringExtra("UID").toString() // get uid string from the previous page

                    for (item in snapshot.children){
                        if (item.key == uid){
                            edtWeightValue.setText(item.child("WEIGHT").value.toString()) //setting values to the fields with the data based on the uid passed that stored in the Database

                            if (item.child("GENDER").value.toString() == "Male"){
                                grpGender.check(R.id.rtnMaleBurnt)
                            }
                            else if (item.child("GENDER").value.toString() == "Female"){
                                grpGender.check(R.id.rtnFemaleBurnt)
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

        spinner() //activate spinner function
    }


    private fun reload(){ //refresh user data
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful) { // if fail to refresh user data
                Toast.makeText(this, "Failed to reload user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun spinner() { //spinner function
        val activity = R.array.activity // getting string array id from string.xml

        val activityAdapter = ArrayAdapter.createFromResource(this,activity,R.layout.support_simple_spinner_dropdown_item)
        activityAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item) // adapter that used to connect spinner with data, filling it with activity string array


        activitySpinner.adapter = activityAdapter //setting the spinner's adapter with activity adapter

        activitySpinner.onItemSelectedListener = this //callback method will activate when user select an item
        typeSpinner.onItemSelectedListener = this

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val activityArray = resources.getStringArray(R.array.activity) // getting string array from string.xml

        /* Various types of adapter with different string arrays*/
        val lightAdapter = ArrayAdapter.createFromResource(this,R.array.light_activity_type,R.layout.support_simple_spinner_dropdown_item)
        lightAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        val moderateAdapter = ArrayAdapter.createFromResource(this,R.array.moderate_activity_type,R.layout.support_simple_spinner_dropdown_item)
        moderateAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        val strenuousAdapter = ArrayAdapter.createFromResource(this,R.array.Strenuous_activity_type,R.layout.support_simple_spinner_dropdown_item)
        strenuousAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        val veryAdapter = ArrayAdapter.createFromResource(this,R.array.Very_strenuous_activity_type,R.layout.support_simple_spinner_dropdown_item)
        veryAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)


        when (parent.getItemAtPosition(position).toString()){ // changes the spinner adapter of typeSpinner while user selects different options of activitySpinner
            activityArray[0] ->  typeSpinner.adapter = lightAdapter
            activityArray[1] ->  typeSpinner.adapter = moderateAdapter
            activityArray[2] ->  typeSpinner.adapter = strenuousAdapter
            activityArray[3] ->  typeSpinner.adapter = veryAdapter
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this,"You haven't select anything yet~",Toast.LENGTH_SHORT).show()
    }

    /* ## Calculate function ## */
    fun calculate (view: View){

            var errorMessage = ""
            val formattedBurnt = String.format("%.2f",cal) // format string to 2 decimal places
            val formattedText = String.format("%s kcal",formattedBurnt) // format result string for display purpose

            if (edtWeightValue.text.isEmpty()){ //check if the weight field is not filled
                errorMessage += "Please Enter Your Weight\n"
            }

            if (grpGender.checkedRadioButtonId == -1){ //check if the gender radioButton is not checked
                errorMessage += "Please Select Your Gender\n"
            }

            if (message == "Activities done : \n"){ //check if user does not adding any activity
                errorMessage += "Please Add Some Activity"
            }

            else if (errorMessage == ""){ // execute below code if all field are filled by checking whether the errorMessage is empty or not / stores user data while finish calculation
                txtCaloriesBurntValue.text = formattedText // display result with formatted text
                dataSave() //save data to the database
                Toast.makeText(this,"Your Data Has Been Saved",Toast.LENGTH_SHORT).show() //notify user while data is saved
            }

            Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show() // show error message


    }

    fun dataSave(){ // function to save data
        val uid = intent.getStringExtra("UID").toString() // get uid string from passes intent
        val currentTime = LocalDateTime.now() //get current local date and time based on your phone
        val date = currentTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) //format current date to mm dd, yyyy format
        val time = currentTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)) //format current time to hh:mm:ss AM/PM format
        val gender = findViewById<RadioButton>(grpGender.checkedRadioButtonId).text.toString() // find current checked radio button id and convert its text to string
        val formattedBurnt = String.format("%.2f",cal)

        myRef.child(uid).child("DATE_BURNT").setValue("$date\t\t$time") // stored current date and time to formatted string
        myRef.child(uid).child("CALORIES_BURNT").setValue(formattedBurnt) // stores calories burnt result to 2 decimal places
        myRef.child(uid).child("WEIGHT").setValue(edtWeightValue.text.toString()) //stores user weight
        myRef.child(uid).child("GENDER").setValue(gender) //stores user gender
    }

    fun add(view: View){ //add item and makes sure the field input is completely filled
    try {
        val maleConstant = 79.378
        val femaleConstant = 63.5
        val activityArray = resources.getStringArray(R.array.activity)
        val addCal = activitySpinner.selectedItem
        val addItem = typeSpinner.selectedItem.toString()
        val weight = edtWeightValue.text.toString().toDouble()
        val weightMultiplierMale = weight/maleConstant
        val weightMultiplierFemale = weight/femaleConstant


        if (grpGender.checkedRadioButtonId == R.id.rtnMaleBurnt){ //checks radiobutton selection
            when (addCal.toString()){
                activityArray[0] -> cal += (300 * weightMultiplierMale)
                activityArray[1] -> cal += (460 * weightMultiplierMale)
                activityArray[2] -> cal += (730 * weightMultiplierMale)
                activityArray[3] -> cal += (920 * weightMultiplierMale)
            }
            findViewById<RadioButton>(R.id.rtnFemaleBurnt).isClickable = false //once user selected a gender and add an item, making another option not clickable so that they cannot change their gender halfway
        }
        else if (grpGender.checkedRadioButtonId == R.id.rtnFemaleBurnt){ //checks radiobutton selection
            when (addCal.toString()){
                activityArray[0] -> cal += (240 * weightMultiplierFemale)
                activityArray[1] -> cal += (370 * weightMultiplierFemale)
                activityArray[2] -> cal += (580 * weightMultiplierFemale)
                activityArray[3] -> cal += (740 * weightMultiplierFemale)
            }
            findViewById<RadioButton>(R.id.rtnMaleBurnt).isClickable = false //once user selected a gender and add an item, making another option not clickable so that they cannot change their gender halfway
        }

        if (grpGender.checkedRadioButtonId == -1){ // send Toast message while the gender radio button is not checked
            Toast.makeText(this,"Please Select Your Gender",Toast.LENGTH_SHORT).show()
        }

        else{  //add item and display added items message if requirements meet
            message+= addItem+"\n"
            txtMulti.text = message
        }
    }
    catch (exception: NumberFormatException){ //catch empty strings if edtWeight.text is null

        var errorMessage = ""

        if (edtWeightValue.text.isEmpty()){ // add errorMessage if user does not fills up the weight value
            errorMessage += "Please Enter Your Weight\n"
        }

        if (grpGender.checkedRadioButtonId == -1){// add errorMessage if user does not check the gender radio button
            errorMessage += "Please Select Your Gender\n"
        }

        Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show() // show errorMessage

    }


    }

    fun reset(view: View) { //reset all the input

        val linear = findViewById<ViewGroup>(R.id.vertical) // find the top vertical linear layout that contains all of the children
        for (i in 0..linear.childCount) { // loops all the children in the layout
            val childViewGroup = linear.getChildAt(i) //get child id based on the index
            if (childViewGroup is ViewGroup) { //identify whether the child is a part of the ViewGroups
                filter(childViewGroup) //filter the children inside the child if it is a part of the ViewGroups
            }
        }
        /* set the specified fields and values to empty*/
        txtCaloriesBurntValue.text = null
        txtMulti.text = null
        cal = 0.0
        message = "Activities done : \n"
    }

    private fun filter(viewGroup: ViewGroup) { //to filter the childViews which also a part of the ViewGroups
        for (i in 0..viewGroup.childCount) {
            val childView = viewGroup.getChildAt(i) // get children inside the ViewGroups
            if (childView is ViewGroup) { // recursive function used to further filter the filtered childViews which is also a part of the ViewGroups till it's completely filtered
                filter(childView)
            }

            if (childView is RadioGroup){ //clear check if the childView is a radioGroup
                childView.clearCheck()
            }
            else{ // clear the input
                when (childView) {
                    is EditText -> childView.text = null
                    is RadioButton -> {
                        childView.isClickable = true
                    }
                }
            }
        }
    }
}


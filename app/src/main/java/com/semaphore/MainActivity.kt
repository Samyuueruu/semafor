package com.semaphore

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import java.util.*

// ENUM CLASS TO INDICATE WHICH PART OF THE CYCLE IS ACTIVE
enum class LightState{
    START, RED, ORANGE, GREEN, REDORANGE
}

class MainActivity : AppCompatActivity() {

    // GLOBAL VARIABLES
    // VARS TO STORE TIMERS FOR EACH SEM CYCLE
    private var nightTimer: Timer? = null
    private var dayTimer: Timer? = null

    // INITIALIZATION OF LIGHSTATE CLASS
    private var state: LightState = LightState.START

    // GLOBAL VAR DELAY USED IN DAYTIMER, 5000 FOR FIRST START
    private var delay: Long = 5000

    // FUNCTION THAT RETURNS REFERENCE TO ImageView TO AVOID MORE GLOBAL VARS
    private fun getImageView(id: Int): ImageView {
        return findViewById(id)
    }

    // UNUSED FUNCTION, RETURNS REFERENCE TO ImageButton
    /*
    private fun getImageButton(id: Int): ImageButton {
        return findViewById(id)
    }*/

    // INITIALIZATION OF GLOBAL VARS, SET TO TRANSPARENT COLOR AS NULL IS NOT VALID
    private var semOff: Drawable? = ColorDrawable(Color.TRANSPARENT)
    private var semRed: Drawable? = ColorDrawable(Color.TRANSPARENT)
    private var semOrange: Drawable? = ColorDrawable(Color.TRANSPARENT)
    private var semGreen: Drawable? = ColorDrawable(Color.TRANSPARENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // STORING REFERENCES TO DRAWABLES
        semOff = ResourcesCompat.getDrawable(resources, R.drawable.semaphore_off, null)
        semRed = ResourcesCompat.getDrawable(resources, R.drawable.semaphore_red, null)
        semOrange = ResourcesCompat.getDrawable(resources, R.drawable.semaphore_orange, null)
        semGreen = ResourcesCompat.getDrawable(resources, R.drawable.semaphore_green, null)

        // STORING REFERENCES TO BUTTONS
        val toggleButton = findViewById<ToggleButton>(R.id.toggleButton)
        val dayCycleButton = findViewById<ImageButton>(R.id.dayModeButton)
        val nightCycleButton = findViewById<ImageButton>(R.id.nightModeButton)

        // REGISTERING TOGGLEBUTTON LISTENER
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            // EXECUTES CODE WHEN BUTTON IS CHECKED/TOGGLED TO STATE ON
            if (isChecked) {
                turnLightsOn()
                // SETS BUTTONS TO CLICKABLE
                nightCycleButton.isClickable = true
                dayCycleButton.isClickable = true
            }
            // EXECUTES CODE WHEN BUTTON IS CHECKED/TOGGLED TO STATE OFF
            else {
                turnLightsOff()
                // SETS BUTTONS TO UNCLICKABLE
                nightCycleButton.isClickable = false
                dayCycleButton.isClickable = false
                cancelDayTimer()
                cancelNightTimer()
            }
        }

        // REGISTERS NIGHTCYCLE BUTTON LISTENER
        nightCycleButton.setOnClickListener {
            cancelDayTimer()
            // RESETS STATE OF DAYCYCLE FOR FURTHER USE
            state = LightState.START
            nightCycle()
        }

        // REGISTERS DAYCYCLE BUTTON LISTENER
        dayCycleButton.setOnClickListener {
            cancelNightTimer()
            dayCycle()
        }

        // SETS BUTTONS TO UNCLICKABLE ON CREATE
        nightCycleButton.isClickable = false
        dayCycleButton.isClickable = false
    }

    // FUNCTION TO CANCEL TIMER AND ALLOW FUTURE USE OF VAR dayTimer
    private fun cancelDayTimer(){
        if(dayTimer != null) {
            dayTimer?.cancel()
            dayTimer = null
        }
    }

    // FUNCTION TO CANCEL TIMER AND ALLOW FUTURE USE OF VAR nightTimer
    private fun cancelNightTimer() {
        if (nightTimer != null){
            nightTimer?.cancel()
            nightTimer = null
        }
    }

    // SETS EACH LIGHT TO ON STATE (ASSIGNS CORRECT ON DRAWABLE)
    private fun turnLightsOn(){
        getImageView(R.id.red).setImageDrawable(semRed)
        getImageView(R.id.orange).setImageDrawable(semOrange)
        getImageView(R.id.green).setImageDrawable(semGreen)
    }

    // SETS EACH LIGHT TO OFF STATE (ASSIGNS CORRECT OFF DRAWABLE)
    private fun turnLightsOff(){
        getImageView(R.id.red).setImageDrawable(semOff)
        getImageView(R.id.orange).setImageDrawable(semOff)
        getImageView(R.id.green).setImageDrawable(semOff)
    }

    // FUNCTION EXECUTES NIGHT SEMAPHORE CYCLE
    private fun nightCycle(){
        // IF STATEMENT TO PREVENT MULTIPLE INSTANCES
        if (nightTimer == null) {
            turnLightsOff()
            // STORES TIMER INSIDE VAR TO CANCEL IT REMOTELY
            nightTimer = Timer()

            // SCHEDULES TIMER FOR 1000ms (1s)
            nightTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    //TOGGLES ORANGE LIGHT FROM OFF TO ON
                    if (getImageView(R.id.orange).drawable.constantState == semOff?.constantState) {
                        getImageView(R.id.orange).setImageDrawable(semOrange)
                    } else {
                        getImageView(R.id.orange).setImageDrawable(semOff)
                    }
                }
            }, 0, 1000)

        }

    }

    // FUNCTION EXECUTES DAY SEMAPHORE CYCLE, FUNCTION CALLS TO ITSELF
    private fun dayCycle(){
        // IF STATEMENT TO PREVENT MULTIPLE INSTANCES
        if (dayTimer == null){

            // START STATE (RED LIGHT ON) OUTSIDE OF TIMER
            // BECAUSE WE RELY ON DELAY RATHER THAN FIXED RATE PERIOD
            if(state == LightState.START){
                // SWITCHES TO STATE RED, SETS LIGHTS CORRESPONDINGLY
                state = LightState.RED
                getImageView(R.id.red).setImageDrawable(semRed)
                getImageView(R.id.orange).setImageDrawable(semOff)
                getImageView(R.id.green).setImageDrawable(semOff)
            }
            // STORES TIMER INSIDE VAR TO CANCEL IT REMOTELY
            dayTimer = Timer()

            // SCHEDULES TIMER WITH DYNAMIC DELAY
            dayTimer?.schedule(object : TimerTask() {
                override fun run() {
                    //WHEN STATEMENT, CALLS FUNCTIONS BASED ON THE STATE CYCLE IS IN
                    when(state){
                        LightState.RED -> redToOrange()
                        LightState.REDORANGE -> orangeToGreen()
                        LightState.GREEN -> greenToOrange()
                        else -> startState()
                    }
                }
            }, delay)
        }
    }

    // FUNCTIONS THAT TOGGLE LIGHTS CORRESPONDINGLY AND SET DELAY TIME FOR TIMER
    // ALSO CANCEL THE TIMER IN WHICH THEY ARE CALLED AND RESET IT, THEN CALL THE FUNCTION
    // THAT WILL INITIATE THE TIMER AGAIN WITH A NEW DELAY
    private fun redToOrange(){
        delay = 2000
        state = LightState.REDORANGE
        getImageView(R.id.orange).setImageDrawable(semOrange)
        getImageView(R.id.green).setImageDrawable(semOff)
        cancelDayTimer()
        dayCycle()
    }

    private fun orangeToGreen(){
        delay = 5000
        state = LightState.GREEN
        getImageView(R.id.red).setImageDrawable(semOff)
        getImageView(R.id.orange).setImageDrawable(semOff)
        getImageView(R.id.green).setImageDrawable(semGreen)
        cancelDayTimer()
        dayCycle()
    }

    private fun greenToOrange(){
        delay = 5000
        state = LightState.ORANGE
        getImageView(R.id.red).setImageDrawable(semOff)
        getImageView(R.id.orange).setImageDrawable(semOrange)
        getImageView(R.id.green).setImageDrawable(semOff)
        cancelDayTimer()
        dayCycle()
    }

    private fun startState(){
        state = LightState.START
        cancelDayTimer()
        dayCycle()
    }

}
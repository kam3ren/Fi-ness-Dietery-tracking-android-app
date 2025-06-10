package com.example.finess

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Calendar

//Home page for pedometer readings/display.
class HomeFragment : Fragment(), SensorEventListener {

    private val PREFS_NAME = "PedometerPrefs"
    private val KEY_LAST_RESET_DATE = "lastResetDate"
    private val KEY_INITIAL_STEPS = "initialSteps"

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private lateinit var stepsTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private var initialSteps = 0
    private var currentSteps = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        stepsTextView = view.findViewById(R.id.steps_taken)
        return view
    }

    override fun onResume() {
        super.onResume()
        checkDailyReset()
        if (sensor == null) {
            Toast.makeText(requireContext(), "Sensor not found", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun checkDailyReset() {
        val today = getCurrentDateString()
        val lastResetDate = sharedPreferences.getString(KEY_LAST_RESET_DATE, "")
        initialSteps = sharedPreferences.getInt(KEY_INITIAL_STEPS, 0)

        if (today != lastResetDate || currentSteps < initialSteps) {
            initialSteps = currentSteps
            sharedPreferences.edit().apply {
                putString(KEY_LAST_RESET_DATE, today)
                putInt(KEY_INITIAL_STEPS, initialSteps)
                apply()
            }
        }
    }

    private fun getCurrentDateString(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format(buildString {
        append("%04d-%02d-%02d")
    }, year, month, day)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            currentSteps = event.values[0].toInt()
            checkDailyReset()
            val dailySteps = currentSteps - initialSteps
            stepsTextView.text = dailySteps.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
package com.example.finess

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.finess.databinding.FragmentWaterBinding
import java.util.Calendar
import androidx.core.content.edit

//Class for water tracking.
class WaterFragment : Fragment() {
    private var _binding: FragmentWaterBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private var currentAmount = 0
    private val dailyGoal = 2000 // Default daily goal in ml

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("WaterPrefs", Context.MODE_PRIVATE)

        checkDailyReset()
        setupProgressBar()
        setupClickListeners()
        updateDisplay()
    }

    private fun checkDailyReset() {
        val today = getCurrentDate()
        val lastSavedDate = sharedPreferences.getString("lastSavedDate", "")
        val savedAmount = sharedPreferences.getInt("waterAmount", 0)

        // Reset if it's a new day
        if (lastSavedDate != today) {
            currentAmount = 0
            this.sharedPreferences.edit() {
                putInt("waterAmount", 0)
                    .putString("lastSavedDate", today)
            }
        } else {
            currentAmount = savedAmount
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-" +
                "${calendar.get(Calendar.MONTH) + 1}-" + // Months are 0-based
                "${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    //Function to start up progress bar activity.
    private fun setupProgressBar() {
        binding.circularProgressBar.apply {
            progressMax = 100f
            setProgressWithAnimation((currentAmount.toFloat() / dailyGoal) * 100, 1000)
        }
    }

    //Function for button activity.
    private fun setupClickListeners() {
        binding.add100mlBtn.setOnClickListener { addWater(100) }
        binding.add250mlBtn.setOnClickListener { addWater(250) }
        binding.add500mlBtn.setOnClickListener { addWater(500) }
    }

    //Function to increase water taken value and update on progress bar.
    private fun addWater(amount: Int) {
        currentAmount += amount
        if (currentAmount > dailyGoal) currentAmount = dailyGoal
        updateDisplay()
        saveProgress()
    }

    @SuppressLint("SetTextI18n")
    private fun updateDisplay() {
        binding.waterTitle.text = "$currentAmount ml / $dailyGoal ml"
        val progress = (currentAmount.toFloat() / dailyGoal) * 100
        binding.circularProgressBar.setProgressWithAnimation(progress, 1000)
    }

    private fun saveProgress() {
        val today = getCurrentDate()
        this.sharedPreferences.edit() {
            putInt("waterAmount", currentAmount)
                .putString("lastSavedDate", today)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
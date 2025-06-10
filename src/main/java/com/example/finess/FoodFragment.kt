package com.example.finess

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.finess.databinding.FragmentFoodBinding
import kotlinx.coroutines.launch
import java.util.Calendar

//Class for calorie tracking.
class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!
    private val apiService = RetrofitClient.apiService

    // User's settings
    private var activityLevel = "Sedentary"
    private var fitnessGoal = "Maintenance"

    // Dynamic goals
    private var maxCALORIES = 2000
    private var maxCARBS = 200
    private var maxFAT = 50
    private var maxPROTEIN = 50

    // Current values
    private var totalCalories = 0
    private var totalCarbs = 0
    private var totalFat = 0
    private var totalProtein = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupProgressBars()
        setupListeners()
        loadSavedData()
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-" +
                "${calendar.get(Calendar.MONTH) + 1}-" +
                "${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun setupSpinners() {
        // Set up activity level spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.activity_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerActivityLevel.adapter = adapter
        }

        // Set up fitness goal spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.fitness_goals,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerFitnessGoal.adapter = adapter
        }

        // Listening for spinner changes
        binding.spinnerActivityLevel.onItemSelectedListener = object : SimpleSpinnerListener() {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                activityLevel = binding.spinnerActivityLevel.getItemAtPosition(position).toString()
                calculateGoals()
            }
        }

        binding.spinnerFitnessGoal.onItemSelectedListener = object : SimpleSpinnerListener() {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                fitnessGoal = binding.spinnerFitnessGoal.getItemAtPosition(position).toString()
                calculateGoals()
            }
        }
    }

    private fun calculateGoals() {
        // Calculations based on selected activity level and fitness goal
        val baseCalories = when (activityLevel) {
            "Sedentary" -> 1800
            "Light" -> 2100
            "Moderate" -> 2400
            "Active" -> 2700
            else -> 2000
        }

        val goalMultiplier = when (fitnessGoal) {
            "Weight Loss" -> 0.85
            "Maintenance" -> 1.0
            "Muscle Gain" -> 1.15
            else -> 1.0
        }

        maxCALORIES = (baseCalories * goalMultiplier).toInt()
        maxCARBS = (maxCALORIES * 0.5 / 4).toInt()  // 50% of calories from carbs
        maxFAT = (maxCALORIES * 0.25 / 9).toInt()   // 25% of calories from fat
        maxPROTEIN = (maxCALORIES * 0.25 / 4).toInt() // 25% of calories from protein

        updateProgressBars()
    }

    private fun setupProgressBars() {
        with(binding) {
            caloriesProgressBar.apply {
                progressMax = 100f
                setProgressWithAnimation(0f)
            }
            carbsProgressBar.progressMax = 100f
            fatProgressBar.progressMax = 100f
            proteinProgressBar.progressMax = 100f
        }
    }

    private fun setupListeners() {
        binding.btnSearch.setOnClickListener {
            val query = binding.etFoodSearch.text.toString()
            if (query.isNotEmpty()) {
                searchFood(query)
            } else {
                showError("Please enter a food item")
            }
        }
    }

    //Function to make food requests via api.
    private fun searchFood(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.searchFood(NutritionQueryRequest(query))
                if (response.foods.isNotEmpty()) {
                    val food = response.foods[0]
                    updateNutritionValues(food)
                    updateProgressBars()
                    saveData()
                } else {
                    showError("No results found")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private fun updateNutritionValues(food: FoodItem) {
        totalCalories += food.nf_calories.toInt()
        totalCarbs += food.nf_total_carbohydrate.toInt()
        totalFat += food.nf_total_fat.toInt()
        totalProtein += food.nf_protein.toInt()
    }

    private fun updateProgressBars() {
        activity?.runOnUiThread {
            with(binding) {
                // Update progress bars
                caloriesProgressBar.setProgressWithAnimation(
                    (totalCalories.toFloat() / maxCALORIES * 100),
                    1000
                )
                carbsProgressBar.setProgressWithAnimation(
                    (totalCarbs.toFloat() / maxCARBS * 100),
                    500
                )
                fatProgressBar.setProgressWithAnimation(
                    (totalFat.toFloat() / maxFAT * 100),
                    500
                )
                proteinProgressBar.setProgressWithAnimation(
                    (totalProtein.toFloat() / maxPROTEIN * 100),
                    500
                )

                // Update text displays
                CalorieCount.text = buildString {
                    append(totalCalories)
                    append(" kcal / ")
                    append(maxCALORIES)
                    append(" kcal")
                }
                carbsCount.text = buildString {
                    append(totalCarbs)
                    append(" g / ")
                    append(maxCARBS)
                    append(" g")
                }
                fatCount.text = buildString {
                    append(totalFat)
                    append(" g / ")
                    append(maxFAT)
                    append(" g")
                }
                proteinCount.text = buildString {
                    append(totalProtein)
                    append(" g / ")
                    append(maxPROTEIN)
                    append(" g")
                }
            }
        }
    }

    private fun saveData() {
        val prefs = requireContext().getSharedPreferences("Nutrition", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putInt("calories", totalCalories)
            putInt("carbs", totalCarbs)
            putInt("fat", totalFat)
            putInt("protein", totalProtein)
            putString("activityLevel", activityLevel)
            putString("fitnessGoal", fitnessGoal)
            putString("lastSavedDate", getCurrentDate())
            apply()
        }
    }

    private fun loadSavedData() {
        val prefs = requireContext().getSharedPreferences("Nutrition", Context.MODE_PRIVATE)
        val lastSavedDate = prefs.getString("lastSavedDate", "")
        val today = getCurrentDate()

        if (lastSavedDate != today) {
            resetDailyValues()
        } else {
            totalCalories = prefs.getInt("calories", 0)
            totalCarbs = prefs.getInt("carbs", 0)
            totalFat = prefs.getInt("fat", 0)
            totalProtein = prefs.getInt("protein", 0)

            // Load spinner selections
            activityLevel = prefs.getString("activityLevel", "Sedentary") ?: "Sedentary"
            fitnessGoal = prefs.getString("fitnessGoal", "Maintenance") ?: "Maintenance"

            // Set spinner positions
            val activityLevels = resources.getStringArray(R.array.activity_levels)
            binding.spinnerActivityLevel.setSelection(activityLevels.indexOf(activityLevel))

            val fitnessGoals = resources.getStringArray(R.array.fitness_goals)
            binding.spinnerFitnessGoal.setSelection(fitnessGoals.indexOf(fitnessGoal))
        }

        calculateGoals()
        updateProgressBars()
    }

    private fun resetDailyValues() {
        totalCalories = 0
        totalCarbs = 0
        totalFat = 0
        totalProtein = 0
        saveData()
    }

    private fun showError(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        saveData()
        super.onDestroyView()
        _binding = null
    }
}

// Helper class for spinner listeners
abstract class SimpleSpinnerListener : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
    override fun onNothingSelected(parent: AdapterView<*>?) {}
}

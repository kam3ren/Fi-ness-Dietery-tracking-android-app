package com.example.finess

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.finess.databinding.FragmentBmiBinding

//Class to calculate body stats.
class BmiFragment : Fragment() {

    private var _binding: FragmentBmiBinding? = null
    val binding get() = _binding!!
    var weight: Int = 50
    var userHeight: Int = 164
    var age: Int = 21
    var selectedGender: String = "Male"
    var selectedEthnicity: String = "Asian"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBmiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSeekBars()
        setupGenderSelection()
        setupSpinner()
        setupCalculateButton()
    }

    private fun setupSeekBars() {
        // Weight SeekBar
        binding.bmiWeightSeekbar.apply {
            max = 200
            progress = weight
            setOnSeekBarChangeListener(createSeekBarListener(
                binding.bmiWeight,
                "Weight(kg):",
                updateValue = { progress -> weight = progress }
            ))
        }

        // Height SeekBar
        binding.bmiHeightSeekbar.apply {
            max = 300
            progress = userHeight
            setOnSeekBarChangeListener(createSeekBarListener(
                binding.bmiHeight,
                "Height(cm):",
                updateValue = { newHeight -> userHeight = newHeight }
            ))
        }

        // Age SeekBar
        binding.ageSeekbar.apply {
            max = 100
            progress = age
            setOnSeekBarChangeListener(createSeekBarListener(
                binding.age,
                "Age:",
                updateValue = { newAge -> age = newAge }
            ))
        }
    }

    private fun setupGenderSelection() {
        binding.genderGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedGender = when (checkedId) {
                R.id.male_gender -> "Male"
                R.id.female_gender -> "Female"
                R.id.nonBinary_gender -> "Non-Binary"
                else -> "Male"
            }
        }
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ethnicity_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerEthnicity.adapter = adapter
        }

        binding.spinnerEthnicity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedEthnicity = parent.getItemAtPosition(position).toString()
                showToast("Selected: $selectedEthnicity")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupCalculateButton() {
        binding.btnCalculate.setOnClickListener {
            calculateBodyComposition()
        }
    }

    fun calculateBodyComposition() {
        if (userHeight <= 0) return

        val heightMeters = userHeight / 100.0
        val boneDensity = calculateBoneDensity()
        val muscleMass = calculateMuscleMass(boneDensity)

        val adjustedBmi = muscleMass / (heightMeters * heightMeters)
        val category = determineCategory(adjustedBmi)

        displayResults(adjustedBmi, muscleMass, boneDensity, category)
    }

    fun calculateBoneDensity(): Double {
        val baseDensity = when (selectedEthnicity) {
            "Asian" -> 1.15
            "Caucasian" -> 1.20
            "African" -> 1.25
            "Hispanic" -> 1.18
            else -> 1.15
        }

        // Age adjustment: Bone density decreases 0.3% per year after 30
        val ageFactor = 1.0 - (0.003 * (age - 30).coerceAtLeast(0)).coerceAtMost(0.3)

        return baseDensity * when (selectedGender) {
            "Male" -> 1.05 * ageFactor
            "Female" -> 0.95 * ageFactor
            else -> 1.0 * ageFactor
        }
    }

    fun calculateMuscleMass(boneDensity: Double): Double {
        val baseMass = weight - boneDensity

        // Age adjustment: Muscle mass decreases 0.5% per year after 40
        val ageAdjustment = when {
            age < 40 -> 1.0
            else -> 1.0 - (0.005 * (age - 40))
        }.coerceAtLeast(0.7)

        return baseMass * ageAdjustment
    }

    fun determineCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Low Muscle Mass"
            bmi < 24.9 -> "Normal Range"
            bmi < 29.9 -> "High Muscle Mass"
            bmi < 34.9 -> "Athletic Build"
            else -> "Exceptional Muscle Mass"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayResults(
        bmi: Double,
        muscleMass: Double,
        boneDensity: Double,
        category: String
    ) {
        binding.bmiResult.text = """
            BMI: ${"%.1f".format(bmi)}
            Muscle Mass: ${"%.1f".format(muscleMass)} kg
            Bone Density: ${"%.1f".format(boneDensity)} kg
            Age: ${this.age} years
            Category: $category
        """.trimIndent()
    }

    private fun createSeekBarListener(
        textView: android.widget.TextView,
        label: String,
        updateValue: (Int) -> Unit
    ) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            textView.text = buildString {
        append(label)
        append(" ")
        append(progress)
    }
            updateValue(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
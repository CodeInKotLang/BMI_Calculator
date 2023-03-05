package com.example.bmicalculator

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.roundToInt

class BMIViewModel : ViewModel() {

    var state by mutableStateOf(BMIScreenState())

    fun onAction(userAction: UserAction) {
        when (userAction) {
            is UserAction.OnGoButtonClicked -> {
                calculateBMI(userAction.context)
            }
            UserAction.OnHeightValueClicked -> {
                state = state.copy(
                    heightValueStage = HeightValueStage.ACTIVE,
                    weightValueStage = WeightValueStage.INACTIVE,
                    shouldBMICardShow = false
                )
            }
            UserAction.OnWeightValueClicked -> {
                state = state.copy(
                    weightValueStage = WeightValueStage.ACTIVE,
                    heightValueStage = HeightValueStage.INACTIVE,
                    shouldBMICardShow = false
                )
            }
            is UserAction.OnNumberClicked -> {
                enterNumber(userAction.number)
            }
            UserAction.OnAllClearButtonClicked -> allClearToZero()
            UserAction.OnDeleteButtonClicked -> deleteLastDigit()
            UserAction.OnWeightTextClicked -> {
                state = state.copy(
                    sheetTitle = "Weight",
                    sheetItemsList = listOf("Kilograms", "Pounds")
                )
            }
            UserAction.OnHeightTextClicked -> {
                state = state.copy(
                    sheetTitle = "Height",
                    sheetItemsList = listOf("Centimeter", "Meter", "Feet", "Inches")
                )
            }
            is UserAction.OnSheetItemClicked -> {
                changeWeightOrHeightUnit(userAction.sheetItem)
            }
        }
    }

    private fun calculateBMI(context: Context) {
        val weightInKgs: Double = when(state.weightUnit) {
            "Pounds" -> state.weightValue.toDouble().times(0.4536)
            else -> state.weightValue.toDouble()
        }
        val heightInMeters: Double = when(state.heightUnit) {
            "Centimeters" -> state.heightValue.toDouble().times(0.01)
            "Feet" -> state.heightValue.toDouble().times(0.3048)
            "Inches" -> state.heightValue.toDouble().times(0.0254)
            else -> state.heightValue.toDouble()
        }
        try {
            val bmiValue = weightInKgs / (heightInMeters * heightInMeters)
            val bmiValueWithDecimal = (bmiValue * 10).roundToInt() / 10.0
            val bmiStage = when(bmiValueWithDecimal) {
                in 0.0..18.5 -> "Underweight"
                in 18.5..25.0 -> "Normal"
                in 25.0..100.0 -> "Overweight"
                else -> "Invalid"
            }
            state = state.copy(
                shouldBMICardShow = true,
                bmi = if (bmiValueWithDecimal > 100) 0.0 else bmiValueWithDecimal,
                bmiStage = bmiStage
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "This BMI does not look good, check again the height and weight value",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun changeWeightOrHeightUnit(sheetItem: String) {
        if (state.sheetTitle == "Weight") {
            state = state.copy(weightUnit = sheetItem)
        } else if (state.sheetTitle == "Height") {
            state = state.copy(heightUnit = sheetItem)
        }
    }

    private fun deleteLastDigit() {
        if (state.weightValueStage != WeightValueStage.INACTIVE) {
            state = state.copy(
                weightValue = if (state.weightValue.length == 1) "0"
                else state.weightValue.dropLast(1)
            )
        } else if (state.heightValueStage != HeightValueStage.INACTIVE) {
            state = state.copy(
                heightValue = if (state.heightValue.length == 1) "0"
                else state.heightValue.dropLast(1)
            )
        }
    }

    private fun allClearToZero() {
        if (state.weightValueStage != WeightValueStage.INACTIVE) {
            state = state.copy(
                weightValue = "0",
                weightValueStage = WeightValueStage.ACTIVE
            )
        } else if (state.heightValueStage != HeightValueStage.INACTIVE) {
            state = state.copy(
                heightValue = "0",
                heightValueStage = HeightValueStage.ACTIVE
            )
        }
    }

    private fun enterNumber(number: String) {
        when {
            state.weightValueStage == WeightValueStage.ACTIVE -> {
                state = state.copy(
                    weightValue = if (number == ".") "0." else number,
                    weightValueStage = WeightValueStage.RUNNING
                )
            }
            state.weightValueStage == WeightValueStage.RUNNING -> {
                if (state.weightValue.contains(".").not()
                    && state.weightValue.length <= 3
                ) {
                    if (state.weightValue.length <= 2 && number != ".") {
                        state = state.copy(
                            weightValue = state.weightValue + number,
                            weightValueStage = WeightValueStage.RUNNING
                        )
                    } else if (number == ".") {
                        state = state.copy(
                            weightValue = state.weightValue + number,
                            weightValueStage = WeightValueStage.RUNNING
                        )
                    }
                } else if (
                    state.weightValue.contains(".") &&
                    state.weightValue.reversed().indexOf(".") < 2
                ) {
                    state = state.copy(
                        weightValue = state.weightValue + number,
                        weightValueStage = WeightValueStage.RUNNING
                    )
                }

            }
            state.heightValueStage == HeightValueStage.ACTIVE -> {
                state = state.copy(
                    heightValue = if (number == ".") "0." else number,
                    heightValueStage = HeightValueStage.RUNNING
                )
            }
            state.heightValueStage == HeightValueStage.RUNNING -> {
                if (state.heightValue.contains(".").not()
                    && state.heightValue.length <= 3
                ) {
                    if (state.heightValue.length <= 2 && number != ".") {
                        state = state.copy(
                            heightValue = state.heightValue + number,
                            heightValueStage = HeightValueStage.RUNNING
                        )
                    } else if (number == ".") {
                        state = state.copy(
                            heightValue = state.heightValue + number,
                            heightValueStage = HeightValueStage.RUNNING
                        )
                    }
                } else if (
                    state.heightValue.contains(".") &&
                    state.heightValue.reversed().indexOf(".") < 2
                ) {
                    state = state.copy(
                        heightValue = state.heightValue + number,
                        heightValueStage = HeightValueStage.RUNNING
                    )
                }
            }
        }
    }
}

sealed class UserAction {
    object OnWeightValueClicked : UserAction()
    object OnHeightValueClicked : UserAction()
    object OnWeightTextClicked : UserAction()
    object OnHeightTextClicked : UserAction()
    data class OnGoButtonClicked(val context: Context) : UserAction()
    data class OnNumberClicked(val number: String) : UserAction()
    object OnAllClearButtonClicked : UserAction()
    object OnDeleteButtonClicked : UserAction()
    data class OnSheetItemClicked(val sheetItem: String) : UserAction()
}
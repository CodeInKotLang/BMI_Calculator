package com.example.bmicalculator

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bmicalculator.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BMIScreen(
    viewModel: BMIViewModel
) {

    val state = viewModel.state
    val context = LocalContext.current

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Hey Guys! Checkout my Body Mass Index: ${state.bmi} BMI," +
                    "which is considered ${state.bmiStage}"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)

    val coroutineScope = rememberCoroutineScope()
    val modalBottomSheet = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    LaunchedEffect(key1 = state.error) {
        if (state.error != null) {
            Toast.makeText(
                context,
                "This BMI does not look good, check again the height and weight value",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheet,
        sheetContent = {
            BottomSheetContent(
                sheetTitle = state.sheetTitle,
                sheetItemsList = state.sheetItemsList,
                onItemClicked = {
                    coroutineScope.launch { modalBottomSheet.hide() }
                    viewModel.onAction(UserAction.OnSheetItemClicked(it))
                },
                onCancelClicked = {
                    coroutineScope.launch { modalBottomSheet.hide() }
                }
            )
        },
        sheetBackgroundColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        content = {
            BMIScreenContent(
                state = state,
                onWeightUnitClicked = {
                    coroutineScope.launch { modalBottomSheet.show() }
                    viewModel.onAction(UserAction.OnWeightTextClicked)
                },
                onHeightUnitClicked = {
                    coroutineScope.launch { modalBottomSheet.show() }
                    viewModel.onAction(UserAction.OnHeightTextClicked)
                },
                onHeightValueClicked = {
                    viewModel.onAction(UserAction.OnHeightValueClicked)
                },
                onWeightValueClicked = {
                    viewModel.onAction(UserAction.OnWeightValueClicked)
                },
                onGoButtonClicked = {
                    viewModel.onAction(UserAction.OnGoButtonClicked)
                },
                onNumberClicked = {
                    viewModel.onAction(UserAction.OnNumberClicked(number = it))
                },
                onACButtonClicked = {
                    viewModel.onAction(UserAction.OnAllClearButtonClicked)
                },
                onDeleteButtonClicked = {
                    viewModel.onAction(UserAction.OnDeleteButtonClicked)
                },
                onShareButtonClicked = {
                    context.startActivity(shareIntent)
                }
            )
        }
    )
}

@Composable
fun BMIScreenContent(
    state: BMIScreenState,
    onWeightUnitClicked: () -> Unit,
    onHeightUnitClicked: () -> Unit,
    onHeightValueClicked: () -> Unit,
    onWeightValueClicked: () -> Unit,
    onGoButtonClicked: () -> Unit,
    onACButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    onShareButtonClicked: () -> Unit,
    onNumberClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBackground)
            .padding(15.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "BMI Calculator",
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnitItem(text = "Weight", onClick = onWeightUnitClicked)
                InputUnitValue(
                    inputValue = state.weightValue,
                    inputUnit = state.weightUnit,
                    inputNoColor =
                    if (state.weightValueStage != WeightValueStage.INACTIVE) {
                        CustomOrange
                    } else Color.Black,
                    onUnitValueClicked = onWeightValueClicked
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnitItem(text = "Height", onClick = onHeightUnitClicked)
                InputUnitValue(
                    inputValue = state.heightValue,
                    inputUnit = state.heightUnit,
                    inputNoColor = if (state.heightValueStage != HeightValueStage.INACTIVE) {
                        CustomOrange
                    } else Color.Black,
                    onUnitValueClicked = onHeightValueClicked
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            Crossfade(targetState = state.shouldBMICardShow) {
                if (it) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        BMIResultCard(
                            bmi = state.bmi,
                            bmiStage = state.bmiStage,
                            bmiStageColor = when(state.bmiStage) {
                                "Underweight" -> CustomBlue
                                "Normal" -> CustomGreen
                                else -> CustomRed
                            }
                        )
                        ShareButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onShareButtonClicked
                        )
                    }
                } else {
                    Divider()
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NumberKeyboard(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(7f),
                            onNumberClick = onNumberClicked
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(3f)
                        ) {
                            SymbolButton(symbol = "AC", onClick = onACButtonClicked)
                            SymbolButtonWithIcon(onClick = onDeleteButtonClicked)
                            SymbolButton(symbol = "GO", onClick = onGoButtonClicked)
                        }
                    }
                }
            }


        }
    }
}


@Preview
@Composable
fun BMIScreenPrev() {

}
package com.flixclusive.presentation.mobile.screens.preferences.settings.dialog_groups.subtitles.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flixclusive.R
import com.flixclusive.domain.preferences.AppSettings
import com.flixclusive.domain.preferences.CaptionSizePreference
import com.flixclusive.domain.preferences.CaptionSizePreference.Companion.getDp
import com.flixclusive.domain.preferences.CaptionStylePreference.Companion.getTextStyle
import com.flixclusive.presentation.mobile.screens.preferences.settings.dialog_groups.subtitles.SubtitleSettingsDialog

@Composable
fun SubtitleDialogSize(
    appSettings: AppSettings,
    onChange: (CaptionSizePreference) -> Unit,
    onDismissRequest: () -> Unit
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(appSettings.subtitleSize) }

    SubtitleSettingsDialog(
        appSettings = appSettings,
        title = stringResource(id = R.string.subtitles_size),
        onDismissRequest = onDismissRequest
    ) {
        Column {
            CaptionSizePreference.values().forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (it == selectedOption),
                            onClick = {
                                onOptionSelected(it)
                                onChange(it)
                            }
                        ),
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (it == selectedOption),
                        onClick = {
                            onOptionSelected(it)
                            onChange(it)
                        }
                    )

                    Text(
                        text = it.toString(),
                        style = appSettings.subtitleFontStyle.getTextStyle().copy(
                            fontSize = it.getDp().sp
                        )
                    )
                }
            }
        }
    }
}
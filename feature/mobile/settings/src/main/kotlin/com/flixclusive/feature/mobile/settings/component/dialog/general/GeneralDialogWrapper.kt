package com.flixclusive.feature.mobile.settings.component.dialog.general

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.flixclusive.core.ui.common.CommonNoticeDialog
import com.flixclusive.feature.mobile.settings.KEY_SEARCH_HISTORY_NOTICE_DIALOG
import com.flixclusive.feature.mobile.settings.SettingsScreenViewModel
import com.flixclusive.core.util.R as UtilR

@Composable
internal fun GeneralDialogWrapper(viewModel: SettingsScreenViewModel) {
//    val context = LocalContext.current
//    val appSettings by rememberLocalAppSettings()
//    val onChangeSettings by rememberSettingsChanger()

    viewModel.run {
        when {
            openedDialogMap[KEY_SEARCH_HISTORY_NOTICE_DIALOG] == true -> {
                CommonNoticeDialog(
                    label = stringResource(UtilR.string.clear_search_history),
                    description = stringResource(UtilR.string.clear_search_history_notice_msg),
                    onConfirm = { clearSearchHistory() },
                    onDismiss = { toggleDialog(KEY_SEARCH_HISTORY_NOTICE_DIALOG) }
                )
            }
        }
    }
}
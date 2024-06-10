package com.flixclusive.feature.mobile.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flixclusive.core.ui.mobile.component.provider.ProviderInstallationStatus
import com.flixclusive.core.util.common.dispatcher.di.ApplicationScope
import com.flixclusive.core.util.common.resource.Resource
import com.flixclusive.core.util.common.ui.UiText
import com.flixclusive.core.util.log.errorLog
import com.flixclusive.data.provider.ProviderManager
import com.flixclusive.domain.provider.GetOnlineProvidersUseCase
import com.flixclusive.domain.updater.ProviderUpdaterUseCase
import com.flixclusive.gradle.entities.ProviderData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.flixclusive.core.util.R as UtilR

@HiltViewModel
class RepositoryScreenViewModel @Inject constructor(
    private val providerManager: ProviderManager,
    private val providerUpdaterUseCase: ProviderUpdaterUseCase,
    private val getOnlineProvidersUseCase: GetOnlineProvidersUseCase,
    @ApplicationScope private val scope: CoroutineScope,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val repository = savedStateHandle.navArgs<RepositoryScreenNavArgs>().repository

    var uiState by mutableStateOf<Resource<List<ProviderData>>>(Resource.Loading)
        private set
    var snackbar by mutableStateOf<Resource.Failure?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set
    val onlineProviderMap = mutableStateMapOf<ProviderData, ProviderInstallationStatus>()

    private var initJob: Job? = null
    private var installJob: Job? = null
    var installAllJob: Job? by mutableStateOf(null)
        private set

    init {
        initialize()
    }

    fun initialize() {
        if (initJob?.isActive == true) {
            return
        }

        initJob = viewModelScope.launch {
            try {
                uiState = Resource.Loading

                val onlineProviders = getOnlineProvidersUseCase(repository)
                onlineProviderMap.clear()

                uiState = onlineProviders
                if (onlineProviders is Resource.Success) {
                    onlineProviders.data!!.forEach { provider ->
                        var providerInstallationStatus = ProviderInstallationStatus.NotInstalled

                        val isInstalledAlready =
                            providerManager.providerDataList.any { it.name.equals(provider.name, true) }

                        if (isInstalledAlready && providerUpdaterUseCase.isProviderOutdated(provider)) {
                            providerInstallationStatus = ProviderInstallationStatus.Outdated
                        } else if (isInstalledAlready) {
                            providerInstallationStatus = ProviderInstallationStatus.Installed
                        }

                        onlineProviderMap[provider] = providerInstallationStatus
                    }
                }
            } catch (e: Exception) {
                errorLog(e.stackTraceToString())
                uiState = Resource.Failure(e.localizedMessage)
            }
        }
    }

    fun installAll() {
        if (installAllJob?.isActive == true) {
            return
        }

        installAllJob = scope.launch {
            val failedToInstallProviders = arrayListOf<String>()

            onlineProviderMap.forEach { (data, state) ->
                if (state == ProviderInstallationStatus.Installed)
                    return@forEach

                if (!installProvider(data)) {
                    failedToInstallProviders.add(data.name)
                    return@forEach
                }
            }

            if (failedToInstallProviders.isNotEmpty()) {
                val failedProviders = failedToInstallProviders.joinToString(", ")

                snackbar = Resource.Failure(UiText.StringResource(UtilR.string.failed_to_load_provider, failedProviders))
                return@launch
            }

            snackbar = Resource.Failure(UiText.StringResource(UtilR.string.all_providers_installed))
        }
    }

    fun toggleInstallationStatus(providerData: ProviderData) {
        if (installJob?.isActive == true) {
            return
        }

        installJob = scope.launch {
            when (onlineProviderMap[providerData]) {
                ProviderInstallationStatus.NotInstalled -> installProvider(providerData)
                ProviderInstallationStatus.Installed -> uninstallProvider(providerData)
                ProviderInstallationStatus.Outdated -> updateProvider(providerData)
                else -> Unit
            }
        }
    }

    private suspend fun updateProvider(providerData: ProviderData) {
        val isSuccess = providerUpdaterUseCase.updateProvider(providerData.name)

        if (isSuccess) {
            onlineProviderMap[providerData] = ProviderInstallationStatus.Installed
        } else {
            snackbar =
                Resource.Failure(UiText.StringResource(UtilR.string.failed_to_update_provider))
        }
    }

    private suspend fun installProvider(providerData: ProviderData): Boolean {
        onlineProviderMap[providerData] = ProviderInstallationStatus.Installing

        try {
            providerManager.loadProvider(
                providerData = providerData,
                needsDownload = true
            )
        } catch (_: Exception) {
            snackbar = Resource.Failure(UiText.StringResource(UtilR.string.failed_to_load_provider, providerData.name))
            onlineProviderMap[providerData] = ProviderInstallationStatus.NotInstalled
            return false
        }

        onlineProviderMap[providerData] = ProviderInstallationStatus.Installed
        return true
    }

    private suspend fun uninstallProvider(providerData: ProviderData) {
        providerManager.unloadProvider(providerData)
        onlineProviderMap[providerData] = ProviderInstallationStatus.NotInstalled
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun onConsumeSnackbar() {
        snackbar = null
    }
}

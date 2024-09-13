package com.flixclusive.core.ui.common.navigation.navigator

import com.flixclusive.gradle.entities.ProviderData

interface ProviderInfoNavigator : RepositorySearchScreenNavigator, ProviderTestNavigator {
    fun seeWhatsNew(providerData: ProviderData)
    fun openProviderSettings(providerData: ProviderData)
}
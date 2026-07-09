package com.ai3dstudio.mobile.feature.assets.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai3dstudio.mobile.core.domain.model.AssetCategory
import com.ai3dstudio.mobile.core.domain.model.LibraryAsset
import com.ai3dstudio.mobile.core.domain.usecase.ObserveAssetsUseCase
import com.ai3dstudio.mobile.core.domain.usecase.SearchAssetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AssetLibraryUiState(
    val query: String = "",
    val selectedCategory: AssetCategory? = null,
    val assets: List<LibraryAsset> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AssetLibraryViewModel @Inject constructor(
    observeAssetsUseCase: ObserveAssetsUseCase,
    private val searchAssetsUseCase: SearchAssetsUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<AssetCategory?>(null)

    private val allAssets = observeAssetsUseCase()

    val uiState: StateFlow<AssetLibraryUiState> = combine(
        query, selectedCategory, allAssets
    ) { q, category, assets ->
        val filtered = assets
            .filter { category == null || it.category == category }
            .filter { q.isBlank() || it.name.contains(q, ignoreCase = true) }
        AssetLibraryUiState(query = q, selectedCategory = category, assets = filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssetLibraryUiState())

    fun onQueryChanged(value: String) { query.value = value }
    fun onCategorySelected(category: AssetCategory?) { selectedCategory.value = category }
}

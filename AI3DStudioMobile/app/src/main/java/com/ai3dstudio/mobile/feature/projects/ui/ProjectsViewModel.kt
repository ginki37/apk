package com.ai3dstudio.mobile.feature.projects.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai3dstudio.mobile.core.domain.model.Project
import com.ai3dstudio.mobile.core.domain.model.ProjectType
import com.ai3dstudio.mobile.core.domain.usecase.CreateProjectUseCase
import com.ai3dstudio.mobile.core.domain.usecase.DeleteProjectUseCase
import com.ai3dstudio.mobile.core.domain.usecase.ObserveProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    observeProjectsUseCase: ObserveProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase
) : ViewModel() {

    val projects: StateFlow<List<Project>> = observeProjectsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createProject(title: String, type: ProjectType, onCreated: (Project) -> Unit) {
        viewModelScope.launch {
            val project = createProjectUseCase(title.ifBlank { "مشروع جديد" }, type)
            onCreated(project)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch { deleteProjectUseCase(id) }
    }
}

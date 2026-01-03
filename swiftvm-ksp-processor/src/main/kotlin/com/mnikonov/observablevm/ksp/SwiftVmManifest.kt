package com.mnikonov.observablevm.ksp

import kotlinx.serialization.Serializable

@Serializable
internal data class SwiftVmManifest(
    val version: Int = 1,
    val viewModels: List<SwiftVmEntry>,
)

@Serializable
internal data class SwiftVmEntry(
    val className: String,
    val simpleName: String,
    val state: StateEntry? = null,
    val actions: List<ActionEntry> = emptyList(),
)

@Serializable
internal data class EventEntry(
    val property: String,
    val typeSimpleName: String,
)

@Serializable
internal data class StateEntry(
    val property: String,
    val typeSimpleName: String,
)

@Serializable
internal data class ActionEntry(
    val function: String,
    val params: List<ActionParamsEntry>,
)

@Serializable
internal data class ActionParamsEntry(
    val paramLabel: String,
    val typeSimpleName: String,
)


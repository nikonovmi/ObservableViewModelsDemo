package com.mnikonov.observablevm

import com.mnikonov.observablevm.swiftvm.SwiftUiAction
import com.mnikonov.observablevm.swiftvm.SwiftUiState
import com.mnikonov.observablevm.swiftvm.SwiftViewModel
import com.mnikonov.observablevm.domain.CaptchaChallenge
import com.mnikonov.observablevm.domain.CaptchaImage
import com.mnikonov.observablevm.domain.CaptchaType
import com.mnikonov.observablevm.domain.randomChallenge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SwiftViewModel
class CaptchaViewModel : ViewModel() {
    private val _challengeState = MutableStateFlow(ChallengeState.ACTIVE)
    private val _data = MutableStateFlow(
        ViewModelState(challenge = randomChallenge(), selected = emptySet()),
    )

    @SwiftUiState
    val uiState = combine(
        _data,
        _challengeState,
    ) { data, challengeState ->
        when (challengeState) {
            ChallengeState.LOADING -> CaptchaViewState.Loading
            ChallengeState.SUCCESS -> CaptchaViewState.Success
            ChallengeState.FAILED -> CaptchaViewState.Failed
            ChallengeState.ACTIVE,
            ChallengeState.VERIFYING -> CaptchaViewState.Active(
                images = data.challenge.images.map { image ->
                    CaptchaImageUiModel(image, isSelected = image in data.selected)
                },
                promptText = when (data.challenge.type) {
                    CaptchaType.BUS -> "Select all images with buses."
                    CaptchaType.GUITAR -> "Select all images with guitars."
                    CaptchaType.DOG -> "Select all images with dogs."
                },
                isVerifying = challengeState == ChallengeState.VERIFYING,
            )
        }
    }.stateIn(coroutineScope, SharingStarted.Lazily, CaptchaViewState.Loading)

    @SwiftUiAction
    fun onAction(action: CaptchaViewAction) {
        when (action) {
            is CaptchaViewAction.ToggleImage -> onImageClicked(action.image)
            is CaptchaViewAction.Submit -> onSubmitClicked()
            is CaptchaViewAction.Retry -> onRetryClicked()
        }
    }

    private fun onImageClicked(image: CaptchaImage) {
        _data.update { data ->
            if (image in data.selected) {
                data.copy(selected = data.selected.minus(image))
            } else {
                data.copy(selected = data.selected.plus(image))
            }
        }
    }

    private fun onSubmitClicked() {
        coroutineScope.launch {
            _challengeState.update { ChallengeState.VERIFYING }
            val data = _data.value
            delay(2000L)

            val expectedResult = data.challenge.images
                .filter { it.type == data.challenge.type }
                .toSet()
            if (data.selected == expectedResult) {
                _challengeState.update { ChallengeState.SUCCESS }
            } else {
                _challengeState.update { ChallengeState.FAILED }
            }
        }
    }

    private fun onRetryClicked() {
        coroutineScope.launch {
            _challengeState.update { ChallengeState.LOADING }
            delay(1000L)
            _data.update { ViewModelState(challenge = randomChallenge(), selected = emptySet()) }
            _challengeState.update { ChallengeState.ACTIVE }
        }
    }
}

private data class ViewModelState(
    val challenge: CaptchaChallenge,
    val selected: Set<CaptchaImage>,
)

private enum class ChallengeState {
    LOADING, ACTIVE, SUCCESS, FAILED, VERIFYING,
}

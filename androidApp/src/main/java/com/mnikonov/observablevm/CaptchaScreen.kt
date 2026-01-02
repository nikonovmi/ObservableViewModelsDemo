@file:OptIn(ExperimentalFoundationApi::class)

package com.mnikonov.observablevm

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnikonov.observablevm.android.R
import com.mnikonov.observablevm.domain.CaptchaImage

@Composable
fun CaptchaScreen(
    viewModel: CaptchaViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = modifier.fillMaxSize()) {
        when (val currentState = state) {
            CaptchaViewState.Loading -> CaptchaLoading()

            is CaptchaViewState.Active -> CaptchaActive(
                state = currentState,
                onToggle = { viewModel.onAction(CaptchaViewAction.ToggleImage(it)) },
                onSubmit = { viewModel.onAction(CaptchaViewAction.Submit) },
            )

            CaptchaViewState.Failed -> CaptchaResult(
                title = "Verification failed",
                body = "Your selection didn’t match. Try again with a new challenge.",
                primaryButton = "Try again",
                onPrimary = { viewModel.onAction(CaptchaViewAction.Retry) },
                tone = ResultTone.Negative,
            )

            CaptchaViewState.Success -> CaptchaResult(
                title = "Verified",
                body = "Thanks! You passed the captcha.",
                primaryButton = "Continue",
                onPrimary = { /* navigate forward */ },
                tone = ResultTone.Positive,
                secondaryButton = "New challenge",
                onSecondary = { viewModel.onAction(CaptchaViewAction.Retry) },
            )
        }
    }
}

@Composable
private fun CaptchaLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Loading challenge…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CaptchaActive(
    state: CaptchaViewState.Active,
    onToggle: (CaptchaImage) -> Unit,
    onSubmit: () -> Unit,
) {

    Box(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Captcha",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.promptText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.images, key = { it.image.name }) { item ->
                    CaptchaImageTile(
                        model = item,
                        enabled = !state.isVerifying,
                        onClick = { onToggle(item.image) },
                    )
                }
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !state.isVerifying,
            ) {
                if (state.isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Verifying…")
                } else {
                    Text("Verify")
                }
            }
        }
    }
}

@Composable
private fun CaptchaImageTile(
    model: CaptchaImageUiModel,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val borderColor =
        if (model.isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .border(width = 2.dp, color = borderColor, shape = shape)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Image(
            painter = painterResource(id = model.image.toDrawableRes()),
            contentDescription = model.image.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        if (model.isSelected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            )
            // simple check marker
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Text(
                    text = "✓",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private enum class ResultTone { Positive, Negative }

@Composable
private fun CaptchaResult(
    title: String,
    body: String,
    primaryButton: String,
    onPrimary: () -> Unit,
    tone: ResultTone,
    secondaryButton: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    val accent =
        if (tone == ResultTone.Positive) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = if (tone == ResultTone.Positive) "✓" else "!",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        color = accent,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(body, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onPrimary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(primaryButton)
                }

                if (secondaryButton != null && onSecondary != null) {
                    OutlinedButton(
                        onClick = onSecondary,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(secondaryButton)
                    }
                }
            }
        }
    }
}

@DrawableRes
private fun CaptchaImage.toDrawableRes(): Int = when (this) {
    CaptchaImage.Bus1 -> R.drawable.bus_1
    CaptchaImage.Bus2 -> R.drawable.bus_2
    CaptchaImage.Bus3 -> R.drawable.bus_3
    CaptchaImage.Bus4 -> R.drawable.bus_4

    CaptchaImage.Guitar1 -> R.drawable.guitar_1
    CaptchaImage.Guitar2 -> R.drawable.guitar_2
    CaptchaImage.Guitar3 -> R.drawable.guitar_3
    CaptchaImage.Guitar4 -> R.drawable.guitar_4

    CaptchaImage.Dog1 -> R.drawable.dog_1
    CaptchaImage.Dog2 -> R.drawable.dog_2
    CaptchaImage.Dog3 -> R.drawable.dog_3
    CaptchaImage.Dog4 -> R.drawable.dog_4

    CaptchaImage.Random1 -> R.drawable.random_1
    CaptchaImage.Random2 -> R.drawable.random_2
    CaptchaImage.Random3 -> R.drawable.random_3
    CaptchaImage.Random4 -> R.drawable.random_4
    CaptchaImage.Random5 -> R.drawable.random_5
    CaptchaImage.Random6 -> R.drawable.random_6
}

package org.jellyfin.androidtv.ui.screensaver

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import kotlinx.coroutines.android.awaitFrame
import org.jellyfin.androidtv.ui.ScreensaverViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random
import org.jellyfin.androidtv.R

@Composable
fun InAppScreensaver() {
    val screensaverViewModel = koinViewModel<ScreensaverViewModel>()
    val visible by screensaverViewModel.visible.collectAsState()

    if (!visible) return

    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val logoSize = 150.dp
    val logoSizePx = with(density) { logoSize.toPx() }

    var position by remember { mutableStateOf(Offset.Zero) }
    var velocity by remember { mutableStateOf(Offset(6f, 4f)) }

    // Initiale Positionierung bei Größenänderung
    LaunchedEffect(screenSize) {
        if (screenSize != IntSize.Zero) {
            position = Offset(
                Random.nextFloat() * (screenSize.width - logoSizePx),
                Random.nextFloat() * (screenSize.height - logoSizePx)
            )
        }
    }

    // EXAKTE BEWEGUNGSLOGIK mit awaitFrame() für bessere FPS-Steuerung
    LaunchedEffect(Unit) {
        while (true) {
            awaitFrame() // Präzisere Framerate statt delay(16L)

            val newX = position.x + velocity.x
            val newY = position.y + velocity.y

            velocity = velocity.copy(
                x = if (newX < -logoSizePx * 0.3f || newX + logoSizePx * 0.7f > screenSize.width) -velocity.x else velocity.x,
                y = if (newY < -logoSizePx * 0.3f || newY + logoSizePx * 0.7f > screenSize.height) -velocity.y else velocity.y
            )

            position = Offset(newX, newY)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned { screenSize = it.size }
            .clickable { screensaverViewModel.notifyInteraction(true) }
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_icon_foreground),
            contentDescription = "jellyfin Logo",
            modifier = Modifier
                .size(logoSize)
                .offset {
                    IntOffset(
                        with(density) { position.x.toDp().roundToPx() },
                        with(density) { position.y.toDp().roundToPx() }
                    )
                }
        )
    }
}

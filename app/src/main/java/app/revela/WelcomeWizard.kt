package app.revela

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Asistente de bienvenida de 3 pasos (solo la primera vez; reanudable desde el menu). */
@Composable
fun WelcomeWizard(onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B10))
            .statusBarsPadding()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.3f))

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 3 } + fadeOut())
                }
            },
            label = "welcome"
        ) { p ->
            val (icon, title, body) = when (p) {
                0 -> Triple(
                    Icons.Filled.Palette,
                    stringResource(R.string.welcome_title),
                    stringResource(R.string.welcome_body1)
                )
                1 -> Triple(
                    Icons.Filled.Lock,
                    stringResource(R.string.welcome_title2),
                    stringResource(R.string.welcome_body2)
                )
                else -> Triple(
                    Icons.Filled.TouchApp,
                    stringResource(R.string.welcome_title3),
                    stringResource(R.string.welcome_body3)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFFB388FF),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    title,
                    color = Ui.text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    body,
                    color = Color(0xFF9A9AA6),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(Modifier.weight(0.5f))

        Row(horizontalArrangement = Arrangement.Center) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .size(if (i == page) 10.dp else 7.dp)
                        .background(
                            if (i == page) Color(0xFFB388FF) else Color(0xFF3A3A46),
                            CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onFinish) { Text(stringResource(R.string.skip), color = Color.Gray) }
            Spacer(Modifier.weight(1f))
            Button(onClick = { if (page < 2) page++ else onFinish() }) {
                Text(if (page < 2) stringResource(R.string.next) else stringResource(R.string.start))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

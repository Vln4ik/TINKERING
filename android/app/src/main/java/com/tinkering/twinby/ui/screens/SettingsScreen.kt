package com.tinkering.twinby.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tinkering.twinby.data.ProfilePublic
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.data.Interests
import com.tinkering.twinby.ui.glass.GlassSurface
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(repo: Repository, padding: PaddingValues, onOpenSupport: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val me = remember { mutableStateOf<ProfilePublic?>(null) }
    val edit = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            me.value = repo.me()
        } catch (_: Exception) {
        }
    }

    Column(modifier = Modifier.padding(padding).statusBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Мой профиль",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(Modifier.height(12.dp))

        me.value?.let { p ->
            GlassSurface(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        AsyncImage(
                            model = p.photo_url,
                            contentDescription = p.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(84.dp).clip(CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("${p.gender} · ${p.age}", color = Color.White.copy(alpha = 0.75f))
                            Text(p.about, color = Color.White.copy(alpha = 0.80f), maxLines = 2)
                        }
                    }
                    Text(
                        if (edit.value) "Скрыть редактирование" else "Редактировать",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp).clickable { edit.value = !edit.value }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        if (edit.value && me.value != null) {
            EditProfileForm(
                initial = me.value!!,
                onSave = { name, gender, age, about, interests, photoFile ->
                    scope.launch {
                        try {
                            me.value = repo.updateMe(
                                name = name,
                                gender = gender,
                                age = age,
                                about = about,
                                interests = interests,
                                photoFile = photoFile
                            )
                            edit.value = false
                        } catch (_: Exception) {
                        }
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = onOpenSupport,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Написать в техподдержку") }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                scope.launch {
                    repo.logout(ctx)
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Выйти") }

    }
}



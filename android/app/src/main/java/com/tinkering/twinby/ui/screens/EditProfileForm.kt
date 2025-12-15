package com.tinkering.twinby.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.tinkering.twinby.data.Interests
import com.tinkering.twinby.data.ProfilePublic
import java.io.File
import java.util.UUID

@Composable
fun EditProfileForm(
    initial: ProfilePublic,
    onSave: (
        name: String?,
        gender: String?,
        age: Int?,
        about: String?,
        interests: List<String>?,
        photoFile: File?,
    ) -> Unit,
) {
    val ctx = LocalContext.current

    val name = remember { mutableStateOf(initial.name) }
    val gender = remember { mutableStateOf(initial.gender) }
    val age = remember { mutableStateOf(initial.age.toString()) }
    val about = remember { mutableStateOf(initial.about) }
    val selected = remember { mutableStateOf(initial.interests.toSet()) }
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri.value = uri
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val painter = rememberAsyncImagePainter(photoUri.value ?: initial.photo_url)
            Image(
                painter = painter,
                contentDescription = "photo",
                modifier = Modifier.size(56.dp).clip(CircleShape).clickable { pickPhoto.launch("image/*") }
            )
            Text(
                text = "Сменить фото",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { pickPhoto.launch("image/*") }
            )
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = gender.value,
                onValueChange = { gender.value = it },
                label = { Text("Пол") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = age.value,
                onValueChange = { age.value = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text("Возраст") },
                modifier = Modifier.weight(0.5f)
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = about.value,
            onValueChange = { about.value = it },
            label = { Text("О себе") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))
        Text("Интересы:", color = Color.White)
        Spacer(Modifier.height(6.dp))
        FlowChips(
            items = Interests.LIST,
            selected = selected.value,
            onToggle = { key ->
                selected.value =
                    if (selected.value.contains(key)) selected.value - key else selected.value + key
            }
        )

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val file = photoUri.value?.let { copyUriToTempFile(ctx, it) }
                onSave(
                    name.value.trim(),
                    gender.value.trim(),
                    age.value.toIntOrNull(),
                    about.value.trim(),
                    selected.value.toList(),
                    file
                )
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Сохранить") }
    }
}

private fun copyUriToTempFile(context: Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri) ?: error("Cannot open photo")
    val outFile = File(context.cacheDir, "photo_${UUID.randomUUID()}.jpg")
    input.use { ins -> outFile.outputStream().use { outs -> ins.copyTo(outs) } }
    return outFile
}



package com.tinkering.twinby.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.tinkering.twinby.data.Interests
import com.tinkering.twinby.data.Repository
import com.tinkering.twinby.data.TokenStore
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.util.UUID

@Composable
fun AuthScreen(
    repo: Repository,
    tokenStore: TokenStore,
    onAuthed: () -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val isRegister = remember { mutableStateOf(true) } // first run: registration by default
    val error = remember { mutableStateOf<String?>(null) }
    val busy = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.loadToken(ctx)
        if (!tokenStore.getToken().isNullOrBlank()) onAuthed()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegister.value) "Регистрация" else "Вход",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Spacer(Modifier.height(12.dp))

        if (isRegister.value) {
            RegisterForm(
                busy = busy.value,
                onSubmit = { login, password, name, gender, age, about, interests, photoFile ->
                    error.value = null
                    busy.value = true
                    scope.launch {
                        try {
                            repo.register(ctx, login, password, name, gender, age, about, interests, photoFile)
                            onAuthed()
                        } catch (e: Exception) {
                            error.value = extractHttpError(e) ?: (e.message ?: "Ошибка регистрации")
                        } finally {
                            busy.value = false
                        }
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Уже есть аккаунт? Войти",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { isRegister.value = false }
            )
        } else {
            LoginForm(
                busy = busy.value,
                onSubmit = { login, password ->
                    error.value = null
                    busy.value = true
                    scope.launch {
                        try {
                            repo.login(ctx, login, password)
                            onAuthed()
                        } catch (e: Exception) {
                            error.value = extractHttpError(e) ?: (e.message ?: "Ошибка входа")
                        } finally {
                            busy.value = false
                        }
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Нет аккаунта? Зарегистрироваться",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { isRegister.value = true }
            )
        }

        if (error.value != null) {
            Spacer(Modifier.height(10.dp))
            Text(text = error.value!!, color = Color(0xFFFF6B6B))
        }
    }
}

private fun extractHttpError(e: Exception): String? {
    if (e is HttpException) {
        return try {
            val body = e.response()?.errorBody()?.string()
            if (body.isNullOrBlank()) "HTTP ${e.code()}" else "HTTP ${e.code()}: $body"
        } catch (_: Exception) {
            "HTTP ${e.code()}"
        }
    }
    return null
}

@Composable
private fun LoginForm(
    busy: Boolean,
    onSubmit: (login: String, password: String) -> Unit,
) {
    val login = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    OutlinedTextField(
        value = login.value,
        onValueChange = { login.value = it },
        label = { Text("Логин") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = password.value,
        onValueChange = { password.value = it },
        label = { Text("Пароль") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    Button(
        enabled = !busy,
        onClick = { onSubmit(login.value.trim(), password.value) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) { Text("Войти") }
}

@Composable
private fun RegisterForm(
    busy: Boolean,
    onSubmit: (
        login: String,
        password: String,
        name: String,
        gender: String,
        age: Int,
        about: String,
        interests: List<String>,
        photoFile: File,
    ) -> Unit,
) {
    val formError = remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current

    val login = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val gender = remember { mutableStateOf("male") }
    val age = remember { mutableStateOf("18") }
    val about = remember { mutableStateOf("") }
    val selected = remember { mutableStateOf(setOf<String>()) }
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri.value = uri
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        val painter = rememberAsyncImagePainter(photoUri.value)
        Image(
            painter = painter,
            contentDescription = "photo",
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { pickPhoto.launch("image/*") }
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = if (photoUri.value == null) "Выбрать фото (обязательно)" else "Фото выбрано",
            color = Color.White,
            modifier = Modifier.clickable { pickPhoto.launch("image/*") }
        )
    }

    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = login.value,
        onValueChange = { login.value = it },
        label = { Text("Логин (для входа)") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = password.value,
        onValueChange = { password.value = it },
        label = { Text("Пароль") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = name.value,
        onValueChange = { name.value = it },
        label = { Text("Имя") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = gender.value,
            onValueChange = { gender.value = it },
            label = { Text("Пол (male/female/other)") },
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
    Text("Интересы (обязательно):", color = Color.White)
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
        enabled = !busy,
        onClick = {
            formError.value = null
            val uri = photoUri.value
            if (uri == null) {
                formError.value = "Выберите фото (обязательно)"
                return@Button
            }
            val loginV = login.value.trim()
            if (loginV.length < 3) {
                formError.value = "Логин минимум 3 символа"
                return@Button
            }
            if (password.value.length < 6) {
                formError.value = "Пароль минимум 6 символов"
                return@Button
            }
            val nameV = name.value.trim()
            if (nameV.isEmpty()) {
                formError.value = "Имя обязательно"
                return@Button
            }
            val aboutV = about.value.trim()
            if (aboutV.isEmpty()) {
                formError.value = "Поле «О себе» обязательно"
                return@Button
            }
            if (selected.value.isEmpty()) {
                formError.value = "Выберите хотя бы 1 интерес"
                return@Button
            }

            val ageInt = age.value.toIntOrNull() ?: 18
            val file = copyUriToTempFile(ctx, uri)
            onSubmit(
                loginV,
                password.value,
                nameV,
                gender.value.trim(),
                ageInt,
                aboutV,
                selected.value.toList(),
                file
            )
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) { Text("Зарегистрироваться") }

    if (formError.value != null) {
        Spacer(Modifier.height(8.dp))
        Text(text = formError.value!!, color = Color(0xFFFF6B6B))
    }
}

private fun copyUriToTempFile(context: Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri) ?: error("Cannot open photo")
    val outFile = File(context.cacheDir, "photo_${UUID.randomUUID()}.jpg")
    input.use { ins -> outFile.outputStream().use { outs -> ins.copyTo(outs) } }
    return outFile
}



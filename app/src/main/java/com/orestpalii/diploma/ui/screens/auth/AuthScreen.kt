package com.orestpalii.diploma.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orestpalii.diploma.R


@Composable
fun AuthScreen(
    vm: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isPwdVisible by remember { mutableStateOf(false) }

    // Як тільки лист відправлено чи юзер у системі – переходимо далі
    LaunchedEffect(vm.emailVerificationSent, vm.isEmailVerified) {
        // ні в цьому екрані нічого не робимо, навігація – в MainScreen
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // LOGO + Заголовок
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isRegistering) "Реєстрація" else "Вхід",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    label = { Text("Електронна пошта") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                )

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = { Text("Пароль") },
                    singleLine = true,
                    visualTransformation = if (isPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            if (isPwdVisible) "Сховати" else "Показати",
                            Modifier.clickable { isPwdVisible = !isPwdVisible }.
                            padding(end = 8.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                )

                // Помилка
                vm.errorMessage?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                // Інструкція по листу
                if (isRegistering && vm.emailVerificationSent) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Лист для підтвердження відправлено на вашу пошту.")
                        Text(
                            "Я підтвердив пошту",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { vm.checkEmailVerification() }
                        )
                    }
                }

                // Кнопка
                Button(
                    onClick = {
                        if (isRegistering) vm.register(email, password)
                        else              vm.login(email, password)
                    },
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(if (isRegistering) "Зареєструватися" else "Увійти")
                }

                // Перемикач режиму
                Text(
                    text = if (isRegistering) "Вже маєте акаунт? Увійти" else "Немає акаунту? Зареєструватися",
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isRegistering = !isRegistering
                            vm.clearErrors()
                        }
                )
            }
        }
    }
}


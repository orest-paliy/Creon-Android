package com.orestpalii.diploma.ui.screens.createpost

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.orestpalii.diploma.ui.helper.DecoratedTextField

@Suppress("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    authorId: String,
    onFinishPosting: () -> Unit,
    viewModel: CreatePostViewModel = viewModel()
) {
    val context = LocalContext.current

    // Скидаємо стан при першій композиції
    LaunchedEffect(Unit) { viewModel.reset() }

    // Стейти з ViewModel
    val title        by remember { derivedStateOf { viewModel.title } }
    val description  by remember { derivedStateOf { viewModel.description } }
    val prompt       by remember { derivedStateOf { viewModel.prompt } }
    val imageUri     by remember { derivedStateOf { viewModel.imageUri } }
    val imageBitmap  by remember { derivedStateOf { viewModel.imageBitmap } }
    val isUploading  by remember { derivedStateOf { viewModel.isUploading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val didFinish    by remember { derivedStateOf { viewModel.didFinishPosting } }

    // Локальний стан для показу кнопок
    var showCamera     by remember { mutableStateOf(false) }
    var showGallery    by remember { mutableStateOf(false) }
    var showPromptInput by remember { mutableStateOf(false) }

    // Тимчасово зберігаємо URI, який передаємо в камеру
    var tmpCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Ховаємо поле промпту коли є зображення
    LaunchedEffect(imageUri, imageBitmap) {
        if (imageUri != null || imageBitmap != null) showPromptInput = false
    }
    // Після успіху закриваємо екран
    LaunchedEffect(didFinish) {
        if (didFinish) {
            viewModel.resetFinishFlag()
            onFinishPosting()
        }
    }

    // Лончер TakePicture → поверне success і ми приймемо tmpCameraUri
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tmpCameraUri?.let { viewModel.onImageUriSelected(it) }
        }
    }
    // Лончер галереї — повертає Uri
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageUriSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Створення публікації", color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onFinishPosting) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Вибір джерела
                if (imageUri == null && imageBitmap == null) {
                    Text(
                        "Оберіть спосіб завантаження фото",
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val cardColor    = MaterialTheme.colorScheme.surface
                        val contentColor = MaterialTheme.colorScheme.primary

                        Button(
                            onClick = { showCamera = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardColor,
                                contentColor   = contentColor
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Text("Камера")
                            }
                        }

                        Button(
                            onClick = { showGallery = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardColor,
                                contentColor   = contentColor
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Photo, contentDescription = null)
                                Text("Галерея")
                            }
                        }

                        Button(
                            onClick = { showPromptInput = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardColor,
                                contentColor   = contentColor
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null)
                                Text("AI")
                            }
                        }
                    }
                }

                // Поле промпту AI
                if (showPromptInput && imageBitmap == null) {
                    DecoratedTextField(
                        value         = prompt,
                        onValueChange = viewModel::onPromptChange,
                        placeholder   = "Промпт для генерації"
                    )
                    Button(
                        onClick = viewModel::generateImageFromPrompt,
                        Modifier.fillMaxWidth()
                    ) {
                        Text("Згенерувати зображення")
                    }
                }

                // Відображення зображення
                when {
                    imageUri != null -> AsyncImage(
                        model            = imageUri,
                        contentDescription = null,
                        contentScale     = ContentScale.Crop,
                        modifier         = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    imageBitmap != null -> Image(
                        bitmap            = imageBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier          = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                // Назва й опис
                if (imageUri != null || imageBitmap != null) {
                    DecoratedTextField(
                        value         = title,
                        onValueChange = viewModel::onTitleChange,
                        placeholder   = "Назва",
                        modifier      = Modifier.fillMaxWidth()
                    )
                    DecoratedTextField(
                        value         = description,
                        onValueChange = viewModel::onDescriptionChange,
                        placeholder   = "Опис",
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = false,
                        minHeight     = 100.dp
                    )
                    errorMessage?.let {
                        Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { viewModel.createPost(authorId, context) },
                        Modifier.fillMaxWidth()
                    ) {
                        Text("Створити")
                    }
                }
            }

            // Loader
            if (isUploading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        Text("Зачекайте…", color = Color.White)
                    }
                }
            }
        }
    }

    // Запускаємо камеру та галерею
    if (showCamera) {
        LaunchedEffect(Unit) {
            showCamera = false
            val uri = viewModel.provideImageUri(context)
            tmpCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }
    if (showGallery) {
        LaunchedEffect(Unit) {
            showGallery = false
            galleryLauncher.launch("image/*")
        }
    }
}

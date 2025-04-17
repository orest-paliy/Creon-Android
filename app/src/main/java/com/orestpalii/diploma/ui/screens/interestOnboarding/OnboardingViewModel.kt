package com.orestpalii.diploma.ui.screens.interestOnboarding


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orestpalii.diploma.data.service.ChatGPTService
import com.orestpalii.diploma.data.service.UserProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {
    private val _selectedTags  = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags

    private val _isLoading      = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome: StateFlow<Boolean> = _navigateToHome

    val tags = listOf(
        "Графічний дизайн", "Плакатний дизайн", "Брендинг", "Логотипи", "Айдентика", "UI-дизайн", "UX-дизайн", "Вебдизайн", "Мобільний дизайн", "Інтерфейси", "3D-дизайн", "Анімація", "Моушн-дизайн", "Інфографіка",

        //Дизайн середовища
        "Інтерʼєр", "Архітектура", "Дизайн кімнат", "Ландшафтний дизайн", "Декорування", "Текстури й матеріали", "Кольорові палітри", "Організація простору", "Стилі інтерʼєру", "Мінімалізм", "Максималізм", "Скандинавський стиль", "Бохо", "Еко-дизайн",

        //Фото та відео
        "Фотографія", "Портретна зйомка", "Пейзажі", "Стріт-фото", "Аналогова фотографія", "Фільмова естетика", "Фотоколаж", "Відеографія", "Кінематографічне відео", "Кольорокорекція",

        //Ілюстрація та цифрове мистецтво
        "Ілюстрація", "Цифрове мистецтво", "Концепт-арт", "Комікси", "Векторна графіка", "3D-арт", "Піксель-арт", "Ретуш", "Фан-арт", "Малювання на планшеті",

        //Образотворче мистецтво
        "Живопис", "Масло", "Акрил", "Акварель", "Гуаш", "Пастель", "Графіка", "Каліграфія", "Шрифт", "Типографіка", "Іконопис", "Монотипія", "Гравюра",

        //Традиційні техніки та ремесла
        "Кераміка", "Гончарство", "Скульптура", "Ліплення", "Ткацтво", "Вишивка", "В'язання", "Шиття", "Миловаріння", "Свічки ручної роботи", "Флористика", "Натуральне фарбування", "Батик",

        // Креативність та освіта
        "Креативне мислення", "Дизайн-мислення", "Мистецька освіта", "Історія мистецтва", "Культурологія", "Психологія творчості", "Арттерапія",

        // Мода та стиль
        "Мода", "Стілінг", "Тренди", "Макіяж", "Нейл-дизайн", "Бʼюті", "Аксесуари", "Фешн-ідеї", "Одяг своїми руками", "Етична мода", "Апсайклінг",

        // DIY та хендмейд
        "DIY-проєкти", "Рукоділля", "Листівки", "Скрапбукінг", "Планери", "Розклад на тиждень", "Органайзери", "Декор до свят", "Хендмейд подарунки", "Хобі", "Мініатюри", "Моделювання",

        // Стиль життя та натхнення
        "Стиль життя", "Подорожі", "Здоровʼя", "Хюґе", "Сімейний затишок", "Ранкові ритуали", "Музика настрою", "Садівництво", "Екологія", "Устойчиве життя", "Вегетаріанські рецепти", "Естетика дня", "Продуктивність",

        // Технології та експерименти
        "AI-арт", "Генеративне мистецтво", "Код-арт", "Інтерактивний дизайн", "AR/VR", "Медіа-арт", "Мистецтво й технології"
    )

    fun toggleTag(tag: String) {
        _selectedTags.update { set ->
            if (set.contains(tag)) set - tag else set + tag
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startProfileSetup() {
        val chosen = _selectedTags.value
        if (chosen.isEmpty()) return

        _isLoading.value = true
        viewModelScope.launch {
            // 1. Запит embedding
            val prompt = "Користувач обрав інтереси: ${chosen.joinToString(", ")}."
            val embedding = ChatGPTService.generateEmbedding(prompt)
            // 2. Генерація аватару
            val avatarBmp = ChatGPTService.generateImageBase64(tags = chosen.toList())

            if (avatarBmp != null) {
                val email = UserProfileService.currentUserEmail ?: ""
                val uid = UserProfileService.currentUserId ?: ""
                val avatarUrl = UserProfileService.uploadAvatarImage(avatarBmp, uid)
                // 3. Створення профілю
                UserProfileService.createUserProfile(
                    email     = email,
                    interests = chosen.toList(),
                    avatarURL = avatarUrl,
                    embedding = embedding
                )
            }

            _isLoading.value = false
            _navigateToHome.value = true
        }
    }
}

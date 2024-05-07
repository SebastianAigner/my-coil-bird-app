import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BirdsUiState(
    val images: List<BirdImage> = emptyList(),
    val selectedCategory: String? = null,
) {
    val categories = images.map { it.category }.toSet()
    val selectedImages = images.filter { it.category == selectedCategory }
}

class BirdsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BirdsUiState())
    val uiState = _uiState.asStateFlow()

    val httpClient = HttpClient() {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getImages(): List<BirdImage> {
        return httpClient
            .get("https://sebi.io/demo-image-api/pictures.json")
            .body()
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            if (state.selectedCategory == category) {
                state.copy(selectedCategory = null)
            } else {
                state.copy(selectedCategory = category)
            }
        }
    }

    fun updateImages() {
        viewModelScope.launch {
            val images = getImages()
            _uiState.update { state ->
                state.copy(images = images)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}
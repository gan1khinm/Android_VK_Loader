package com.example.vkloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vk.sdk.api.photos.dto.PhotosGetAlbumsResponseDto

data class Album(
    var count: Int,
    var id: Int,
    var title: String,
    var coverUrl: String?
)

class AlbumsViewModel : ViewModel() {
    fun parseAlbumsLoad(result: PhotosGetAlbumsResponseDto): List<Album> =
        result.items.map { album ->
            Album(
                id = album.id,
                count = album.size,
                coverUrl = album.thumbSrc,
                title = album.title
            )
        }
}

class AlbumsViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlbumsViewModel::class.java)) {
            return AlbumsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

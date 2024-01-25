package com.example.vkloader

import android.net.Uri
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKHttpPostCall
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PhotosPostCommand(private val photos: List<Uri>, private val albumId: Int) : ApiCommand<Int>() {

    override fun onExecute(manager: VKApiManager): Int {
        val callBuilder = VKMethodCall.Builder()
            .method("photos.save")
            .args("album_id", albumId)
            .version(manager.config.version)

        val uploadInfo = getServerUploadInfo(manager, albumId)
        val photosUploadInfo = uploadPhotos(photos, uploadInfo, manager)

        callBuilder.args("server", photosUploadInfo.server)
            .args("photos_list", photosUploadInfo.photos)
            .args("hash", photosUploadInfo.hash)

        return manager.execute(callBuilder.build(), ResponseApiParser())
    }

    private fun getServerUploadInfo(manager: VKApiManager, albumId: Int): VKServerUploadInfo {
        val uploadInfoCall = VKMethodCall.Builder()
            .method("photos.getUploadServer")
            .args("album_id", albumId)
            .version(manager.config.version)
            .build()
        return manager.execute(uploadInfoCall, ServerUploadInfoParser())
    }

    private fun uploadPhotos(uris: List<Uri>, serverUploadInfo: VKServerUploadInfo, manager: VKApiManager): VKFileUploadInfo {
        val fileUploadCallBuilder = VKHttpPostCall.Builder()
            .url(serverUploadInfo.uploadUrl)
            .timeout(TimeUnit.MINUTES.toMillis(5))
            .retryCount(RETRY_COUNT)

        uris.forEachIndexed { index, uri ->
            fileUploadCallBuilder.args("file$index", uri, "image.jpg")
        }

        return manager.execute(fileUploadCallBuilder.build(), null, FileUploadInfoParser())
    }

    companion object {
        const val RETRY_COUNT = 3
    }

    private class ResponseApiParser : VKApiJSONResponseParser<Int> {
        override fun parse(responseJson: JSONObject): Int {
            try {
                return responseJson.getJSONArray("response").length()
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

    private class ServerUploadInfoParser : VKApiJSONResponseParser<VKServerUploadInfo> {
        override fun parse(responseJson: JSONObject): VKServerUploadInfo {
            try {
                val joResponse = responseJson.getJSONObject("response")
                return VKServerUploadInfo(
                    uploadUrl = joResponse.getString("upload_url"),
                    albumId = joResponse.getInt("album_id"),
                    userId = joResponse.getInt("user_id")
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

    private class FileUploadInfoParser : VKApiJSONResponseParser<VKFileUploadInfo> {
        override fun parse(responseJson: JSONObject): VKFileUploadInfo {
            try {
                val resp = responseJson.getString("root_response")
                val joResponse = Json.parseToJsonElement(resp).jsonObject
                return VKFileUploadInfo(
                    server = joResponse["server"].toString().removeSurrounding("\"").toInt(),
                    photos = joResponse["photos_list"].toString().removeSurrounding("\"").replace("\\", ""),
                    hash = joResponse["hash"].toString().removeSurrounding("\"")
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}

data class VKFileUploadInfo(val server: Int, val photos: String, val hash: String)
data class VKServerUploadInfo(val uploadUrl: String, val albumId: Int, val userId: Int)

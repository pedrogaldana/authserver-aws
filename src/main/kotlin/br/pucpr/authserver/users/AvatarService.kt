package br.pucpr.authserver.users

import br.pucpr.authserver.exception.UnsupportedMediaTypeException
import br.pucpr.authserver.files.FileStorage
import br.pucpr.authserver.files.MultipartFileResource
import br.pucpr.authserver.utils.MD5Util
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files


@Service
class AvatarService(@Qualifier("fileStorage") val storage: FileStorage) {
    fun save(user: User, avatar: MultipartFile): String =
        try {
            val contentType = avatar.contentType!!
            val extension = when (contentType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                else -> throw UnsupportedMediaTypeException("jpeg", "png")
            }
            val name = "${user.id}.$extension"
            storage.save(user, "$FOLDER/$name", avatar)
            name
        } catch (exception: Error) {
            log.error("Unable to store avatar of user ${user.id}! Using default.", exception)
            DEFAULT_AVATAR
        }

    fun urlFor(path: String) = storage.urlFor("$FOLDER/$path")

    fun load(name: String) = storage.load(name)

    fun delete(avatar: String): Boolean {
        return if (avatar != DEFAULT_AVATAR) {
            val path = "$FOLDER${FileStorage.URL_SEPARATOR}${avatar}"
            storage.delete(path)
        } else
            false
    }

    fun getExternalAvatar(user: User): MultipartFile? {
        try {
            return getFromGravatar(user)
        } catch (httpException: HttpClientErrorException) {
            if (httpException.statusCode == HttpStatus.NOT_FOUND)
                return getFromUiAvatars(user)
        }
        return null
    }

    private fun getFromGravatar(user: User): MultipartFile? {
        val emailTratado = user.email.trim().lowercase()
        val hash = MD5Util.md5Hex(emailTratado)
        val url = "https://gravatar.com/avatar/${hash}.png?d=404&s=128"
        return multipartFileFromUrl(url)
    }

    private fun getFromUiAvatars(user: User): MultipartFile? {
        return try {
            val name = user.name.substringBefore(" ")
            val url = "https://ui-avatars.com/api/?name=$name&background=random&length=1&format=png&size=128&bold=true"
            multipartFileFromUrl(url)
        } catch (e: Exception) {
            log.error("Não foi possível obter o avatar.", e)
            null
        }
    }

    private fun multipartFileFromUrl(url: String): MultipartFile? {
        val restTemplate = RestTemplate()
        val imageBytes = restTemplate.getForObject(url, ByteArray::class.java)
        val tempFile = File.createTempFile("tempImage", ".png")
        Files.write(tempFile.toPath(), imageBytes)
        return MultipartFileResource(tempFile, "teste")
    }

    companion object {
        const val FOLDER = "avatars"
        const val DEFAULT_AVATAR = "default.png"
        private val log = LoggerFactory.getLogger(AvatarService::class.java)
    }
}
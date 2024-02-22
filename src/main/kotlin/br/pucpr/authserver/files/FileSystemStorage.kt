package br.pucpr.authserver.files

import br.pucpr.authserver.files.FileStorage.Companion.URL_SEPARATOR
import br.pucpr.authserver.users.User
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

class FileSystemStorage : FileStorage {
    override fun save(user: User, path: String, file: MultipartFile): String {
        val root = Paths.get(ROOT)
        val destinationFile = root.resolve(path)
            .normalize()
            .toAbsolutePath()

        Files.createDirectories(destinationFile.parent)
        file.inputStream.use {
            Files.copy(it, destinationFile, StandardCopyOption.REPLACE_EXISTING)
        }
        return path
    }

    override fun urlFor(name: String): String =
        "http://localhost:8080/api/files/" +
                URLEncoder.encode(
                    name.replace("/", URL_SEPARATOR),
                    StandardCharsets.UTF_8
                )

    override fun load(path: String): Resource? =
        pathFromString(path)?.let { UrlResource(it.toUri()) }

    override fun delete(path: String): Boolean {
        pathFromString(path)?.let {
            Files.deleteIfExists(it)
            return true
        }
        return false

    }

    private fun pathFromString(path: String): Path? {
        return Paths.get(ROOT, path.replace(URL_SEPARATOR, "/"))
            .takeIf { it.exists() }

    }

    companion object {
        const val ROOT = "./fs"
    }
}
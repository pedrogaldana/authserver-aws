package br.pucpr.authserver.files

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class MultipartFileResource(private val _file: File, private val _fileName: String) : MultipartFile {
    private val filePath: Path = _file.toPath()
    private val contentType = Files.probeContentType(filePath)
    private val _bytes = Files.readAllBytes(filePath)

    override fun getName(): String = _fileName
    override fun getOriginalFilename(): String = _file.name
    override fun getContentType(): String = contentType
    override fun isEmpty(): Boolean = false
    override fun getSize(): Long = bytes.size.toLong()
    override fun getBytes(): ByteArray = _bytes
    override fun getInputStream(): ByteArrayInputStream = ByteArrayInputStream(bytes)
    override fun transferTo(dest: File) {
        dest.writeBytes(bytes)
    }
}

package br.pucpr.authserver.files

import br.pucpr.authserver.files.FileStorage.Companion.URL_SEPARATOR
import br.pucpr.authserver.users.User
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class S3Storage : FileStorage {
    private val s3: AmazonS3 = AmazonS3ClientBuilder.standard()
        .withRegion(Regions.US_EAST_1)
        .withCredentials(EnvironmentVariableCredentialsProvider())
        .build()

    override fun save(user: User, path: String, file: MultipartFile): String {
        val contentType = file.contentType!!

        val transferManager = TransferManagerBuilder.standard()
            .withS3Client(s3)
            .build()

        val meta = ObjectMetadata()
        meta.contentType = contentType
        meta.contentLength = file.size
        meta.userMetadata["userId"] = "${user.id}"
        meta.userMetadata["originalFilename"] = file.originalFilename

        transferManager
            .upload(BUCKET, path, file.inputStream, meta)
            .waitForUploadResult()

        return path
    }

    override fun load(path: String): Resource? {
        val newPath = path.replace(URL_SEPARATOR, "/")
        return InputStreamResource(
            s3.getObject(BUCKET, newPath).objectContent
        )
    }

    override fun delete(path: String): Boolean {
        load(path)?.let {
            val key =  path.replace(URL_SEPARATOR, "/")
            s3.deleteObject(BUCKET, key)
            return true
        }
        return false
    }


    override fun urlFor(name: String) = "https://d16fx89kubrk30.cloudfront.net/$name"

    companion object {
        private const val BUCKET = "pedrogaldana-authserver-public"
    }
}
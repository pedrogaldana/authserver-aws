package br.pucpr.authserver.users

import br.pucpr.authserver.exception.NotFoundException
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/files")
class AvatarsController(var avatarService: AvatarService) {
    @SecurityRequirement(name = "AuthServer")
    @PreAuthorize("permitAll()")
    @GetMapping("/{filename}", produces = ["image/jpg", "image/png"])
    fun serve(@PathVariable filename: String): ResponseEntity<Resource> {
        val contentType = if (filename.endsWith("jpg")) MediaType.IMAGE_JPEG else MediaType.IMAGE_PNG
        return avatarService.load(filename)
            ?.let {
                ResponseEntity.ok()
                    .contentType(contentType)
                    .body(it)
            }
            ?: throw NotFoundException(filename)
    }
}
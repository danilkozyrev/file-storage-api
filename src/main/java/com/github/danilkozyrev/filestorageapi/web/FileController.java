package com.github.danilkozyrev.filestorageapi.web;

import com.github.danilkozyrev.filestorageapi.dto.form.MetadataForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.service.FileService;
import com.github.danilkozyrev.filestorageapi.validation.ValidationGroups;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@Api(tags = "Files")
@ApiResponses({
        @ApiResponse(code = 400, message = "Invalid input"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 404, message = "The specified file doesn't exist or access to it is forbidden"),
        @ApiResponse(code = 500, message = "Internal server error")
})
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(
            path = "/api/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Upload file")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", responseHeaders = @ResponseHeader(
                    name = HttpHeaders.LOCATION,
                    description = "A location uri of the uploaded file",
                    response = String.class)),
            @ApiResponse(
                    code = 404,
                    message = "The specified parent folder doesn't exist or access to it is forbidden"),
            @ApiResponse(code = 413, message = "The request exceeds its maximum permitted size"),
            @ApiResponse(code = 507, message = "The storage limit reached")
    })
    public ResponseEntity<Metadata> uploadFile(
            @Validated(ValidationGroups.Create.class) MetadataForm metadataForm,
            @RequestParam("file") MultipartFile file,
            UriComponentsBuilder uriComponentsBuilder) {
        Metadata fileMetadata = fileService.saveFile(metadataForm, file.getResource());
        URI locationUri = uriComponentsBuilder
                .path("/api/files/{fileId}")
                .buildAndExpand(fileMetadata.getId())
                .toUri();
        return ResponseEntity.created(locationUri).body(fileMetadata);
    }

    @GetMapping(path = "/api/files/{fileId}/link", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Generate a temporary link for file download",
            notes = "After expiration you will get 410 Gone.")
    public Map<String, String> getTemporaryLink(
            @PathVariable("fileId") Long fileId,
            UriComponentsBuilder uriComponentsBuilder) {
        String accessToken = fileService.generateFileAccessToken(fileId);
        String uriString = uriComponentsBuilder.path("/tl/{token}").buildAndExpand(accessToken).toUriString();
        return Map.of("link", uriString);
    }

    @GetMapping(path = "/api/files/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get file metadata")
    public Metadata getFileMetadata(@PathVariable("fileId") Long fileId) {
        return fileService.getFileMetadata(fileId);
    }

    @GetMapping(path = "/api/files/{fileId}/contents", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation("Download file")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") Long fileId) {
        Resource fileResource = fileService.getFileContents(fileId);
        return getFileResponseEntity(fileResource);
    }

    @GetMapping(path = "/tl/{token}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadFileByTemporaryLink(@PathVariable("token") String token) {
        Resource fileResource = fileService.getFileContents(token);
        return getFileResponseEntity(fileResource);
    }

    @PatchMapping(
            path = "/api/files/{fileId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Move and/or rename file")
    @ApiResponse(code = 404, message = "The specified file or parent folder doesn't exist or access to it is forbidden")
    public Metadata updateFileMetadata(
            @PathVariable("fileId") Long fileId,
            @RequestBody @Validated(ValidationGroups.Update.class) MetadataForm metadataForm) {
        return fileService.updateFileMetadata(fileId, metadataForm);
    }

    @DeleteMapping("/api/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Delete file", notes = "Use the permanent parameter to bypass trash.")
    public void deleteFile(
            @PathVariable("fileId") Long fileId,
            @RequestParam(name = "permanent", required = false, defaultValue = "false") boolean permanent) {
        fileService.deleteFile(fileId, permanent);
    }

    private ResponseEntity<Resource> getFileResponseEntity(Resource fileResource) {
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
                .body(fileResource);
    }

}

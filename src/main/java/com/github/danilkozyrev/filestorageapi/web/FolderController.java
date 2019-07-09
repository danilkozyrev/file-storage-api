package com.github.danilkozyrev.filestorageapi.web;

import com.github.danilkozyrev.filestorageapi.dto.form.MetadataForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.security.UserPrincipal;
import com.github.danilkozyrev.filestorageapi.service.FolderService;
import com.github.danilkozyrev.filestorageapi.validation.ValidationGroups;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/folders")
@Api(tags = "Folders")
@ApiResponses({
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 500, message = "Internal server error")
})
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create folder")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", responseHeaders = @ResponseHeader(
                    name = HttpHeaders.LOCATION,
                    description = "A location uri of the created folder",
                    response = String.class)),
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "The specified parent folder doesn't exist or access to it is forbidden")
    })
    public ResponseEntity<Metadata> createFolder(
            @RequestBody @Validated(ValidationGroups.Create.class) MetadataForm metadataForm,
            UriComponentsBuilder uriComponentsBuilder) {
        Metadata folderMetadata = folderService.createFolder(metadataForm);
        URI locationUri = uriComponentsBuilder
                .path("/api/folders/{folderId}")
                .buildAndExpand(folderMetadata.getId())
                .toUri();
        return ResponseEntity.created(locationUri).body(folderMetadata);
    }

    @GetMapping(path = "/{folderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get folder metadata")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "The specified folder doesn't exist or access to it is forbidden")
    })
    public Metadata getFolderMetadata(@PathVariable("folderId") Long folderId) {
        return folderService.getFolderMetadata(folderId);
    }

    @GetMapping(path = "/{folderId}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get folder items")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "The specified folder doesn't exist or access to it is forbidden")
    })
    public List<Metadata> getFolderItems(@PathVariable("folderId") Long folderId) {
        return folderService.getFolderItems(folderId);
    }

    @GetMapping(path = "/root", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get root folder metadata")
    public Metadata getRootFolderMetadata(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return folderService.getRootFolderMetadata(userPrincipal.getId());
    }

    @GetMapping(path = "/root/items", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get root folder items")
    public List<Metadata> getRootFolderItems(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return folderService.getRootFolderItems(userPrincipal.getId());
    }

    @PatchMapping(
            path = "/{folderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Move and/or rename folder")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(
                    code = 404,
                    message = "The specified folder or parent folder doesn't exist or access to it is forbidden"),
            @ApiResponse(code = 422, message = "A circular reference in the folder tree occurs")
    })
    public Metadata updateFolderMetadata(
            @PathVariable("folderId") Long folderId,
            @RequestBody @Validated(ValidationGroups.Update.class) MetadataForm metadataForm) {
        return folderService.updateFolderMetadata(folderId, metadataForm);
    }

    @DeleteMapping("/{folderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Delete folder", notes = "Use the permanent parameter to bypass trash.")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "The specified folder doesn't exist or access to it is forbidden")
    })
    public void deleteFolder(
            @PathVariable("folderId") Long folderId,
            @RequestParam(name = "permanent", required = false, defaultValue = "false") boolean permanent) {
        folderService.deleteFolder(folderId, permanent);
    }

}

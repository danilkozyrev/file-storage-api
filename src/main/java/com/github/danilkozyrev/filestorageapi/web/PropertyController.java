package com.github.danilkozyrev.filestorageapi.web;

import com.github.danilkozyrev.filestorageapi.dto.form.PropertyForm;
import com.github.danilkozyrev.filestorageapi.dto.form.ValidSet;
import com.github.danilkozyrev.filestorageapi.dto.projection.PropertyInfo;
import com.github.danilkozyrev.filestorageapi.service.PropertyService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/files/{fileId}/properties")
@Api(tags = "Properties")
@ApiResponses({
        @ApiResponse(code = 400, message = "Invalid input"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 404, message = "The specified file doesn't exist or access to it is forbidden"),
        @ApiResponse(code = 500, message = "Internal server error")
})
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Save file properties",
            notes = "If a property with the given key exists then it's value is updated, otherwise creates a new " +
                    "property.")
    public List<PropertyInfo> saveProperties(
            @PathVariable("fileId") Long fileId,
            @RequestBody @Valid ValidSet<PropertyForm> propertySet) {
        return propertyService.saveProperties(fileId, propertySet);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get file properties")
    public List<PropertyInfo> getProperties(@PathVariable("fileId") Long fileId) {
        return propertyService.getProperties(fileId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Delete all file properties")
    public void deleteProperties(@PathVariable("fileId") Long fileId) {
        propertyService.deleteProperties(fileId);
    }

}

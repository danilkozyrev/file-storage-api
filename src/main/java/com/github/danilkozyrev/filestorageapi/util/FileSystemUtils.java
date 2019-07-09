package com.github.danilkozyrev.filestorageapi.util;

import com.github.danilkozyrev.filestorageapi.exception.FileSystemException;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.springframework.core.io.InputStreamSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

/**
 * Utility class for working with the file system.
 */
public final class FileSystemUtils {

    private FileSystemUtils() {
    }

    /**
     * Saves a file to the given location and create all required parent directories.
     *
     * @param fileInputStreamSource source of the file contents.
     * @param location              file will be saved to this location.
     * @throws FileSystemException if an I/O error occurs.
     */
    public static void saveFile(InputStreamSource fileInputStreamSource, String location) {
        Path filePath = Paths.get(location);
        try (InputStream fileInputStream = fileInputStreamSource.getInputStream()) {
            Files.createDirectories(filePath.getParent());
            Files.copy(fileInputStream, filePath);
        } catch (IOException exception) {
            throw new FileSystemException(exception);
        }
    }

    /**
     * Deletes a file with the given location.
     *
     * @param fileLocation location of the file.
     * @throws FileSystemException      if an I/O error occurs.
     * @throws IllegalArgumentException if file is a directory.
     */
    public static void deleteFile(String fileLocation) {
        Path filePath = Paths.get(fileLocation);
        try {
            if (Files.isDirectory(filePath)) {
                throw new IllegalArgumentException(filePath + " is not a file");
            } else {
                Files.delete(filePath);
            }
        } catch (IOException exception) {
            throw new FileSystemException(exception);
        }
    }

    /**
     * Detects file content type using it's name and contents. The method checks the file extension first and returns
     * the MIME type based on that, if it finds a result. Otherwise the type detection is based on the content of the
     * given form stream.
     *
     * @param fileName              the name of the file.
     * @param fileInputStreamSource source of the file contents.
     * @return detected content type.
     * @throws FileSystemException if the underlying resource doesn't exist or the content stream could not be opened.
     */
    public static String detectContentType(String fileName, InputStreamSource fileInputStreamSource) {
        Tika tika = new Tika();

        String fileNameDetect = tika.detect(fileName);
        if (!fileNameDetect.equals(MimeTypes.OCTET_STREAM)) {
            return fileNameDetect;
        }

        try (InputStream fileInputStream = fileInputStreamSource.getInputStream()) {
            return tika.detect(fileInputStream);
        } catch (IOException exception) {
            throw new FileSystemException(exception);
        }
    }

}

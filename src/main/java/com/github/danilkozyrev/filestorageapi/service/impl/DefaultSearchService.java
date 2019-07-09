package com.github.danilkozyrev.filestorageapi.service.impl;

import com.github.danilkozyrev.filestorageapi.domain.*;
import com.github.danilkozyrev.filestorageapi.dto.form.SearchForm;
import com.github.danilkozyrev.filestorageapi.dto.projection.Metadata;
import com.github.danilkozyrev.filestorageapi.exception.RecordNotFoundException;
import com.github.danilkozyrev.filestorageapi.mapper.ItemProbeMapper;
import com.github.danilkozyrev.filestorageapi.mapper.MetadataMapper;
import com.github.danilkozyrev.filestorageapi.persistence.*;
import com.github.danilkozyrev.filestorageapi.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor()
public class DefaultSearchService implements SearchService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final ItemProbeMapper itemProbeMapper;
    private final MetadataMapper metadataMapper;

    @Override
    public List<Metadata> findItems(SearchForm searchForm, Long ownerId) {
        if (userRepository.existsById(ownerId)) {
            File fileProbe = itemProbeMapper.mapToFileProbe(searchForm, ownerId);
            List<File> foundFiles = fileRepository.findAll(Example.of(fileProbe, getMatcher()));
            if (searchForm.getMimeType() != null) {
                return metadataMapper.mapFiles(foundFiles);
            } else {
                Folder folderProbe = itemProbeMapper.mapToFolderProbe(searchForm, ownerId);
                List<Folder> foundFolders = folderRepository.findAll(Example.of(folderProbe, getMatcher()));
                return metadataMapper.mapItems(foundFolders, foundFiles);
            }
        } else {
            throw new RecordNotFoundException(User.class, ownerId);
        }
    }

    @Override
    public List<Metadata> findRecentItems(Instant afterDate, Long ownerId) {
        if (userRepository.existsById(ownerId)) {
            List<Folder> recentFolders = folderRepository.findFoldersByDateModifiedAfterAndOwnerId(afterDate, ownerId);
            List<File> recentFiles = fileRepository.findFilesByDateModifiedAfterAndOwnerId(afterDate, ownerId);
            return metadataMapper.mapItems(recentFolders, recentFiles);
        } else {
            throw new RecordNotFoundException(User.class, ownerId);
        }
    }

    private ExampleMatcher getMatcher() {
        return ExampleMatcher
                .matchingAll()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
    }

}

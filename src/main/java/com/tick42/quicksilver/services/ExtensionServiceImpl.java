package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.services.base.ExtensionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@Service
public class ExtensionServiceImpl implements ExtensionService {
    private final ExtensionRepository extensionRepository;
    private final Map<Long, Extension> featured = Collections.synchronizedMap(new LinkedHashMap<>());
    private final List<Extension> mostRecent = Collections.synchronizedList(new ArrayList<>());
    private final int mostRecentQueueLimit = 5;
    private int featuredLimit = 5;

    public ExtensionServiceImpl(ExtensionRepository extensionRepository) {
        this.extensionRepository = extensionRepository;
        updateMostRecent();
        loadFeatured();
    }

    @Override
    public Extension findById(long extensionId, UserDetails loggedUser) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));

        UserModel owner = extension.getOwner();
        if(extension.isPending() && (loggedUser == null || (!AuthorityUtils.authorityListToSet(loggedUser.getAuthorities()).contains("ROLE_ADMIN") &&
                (loggedUser.getId() != owner.getId() || !owner.isActive())))){
            throw new UnauthorizedException("Extension is not available.");
        }

        return extension;
    }

    @Override
    public Extension update(Extension newExtension) {
        extensionRepository.save(newExtension);
        reloadExtension(newExtension);

        return newExtension;
    }

    @Override
    public Extension save(Extension extension){
        return extensionRepository.save(extension);
    }

    @Override
    public Extension delete(long extensionId, UserModel loggedUser) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));

        UserModel owner = extension.getOwner();
        if(loggedUser.getId() != owner.getId() && !loggedUser.getRole().equals("ROLE_ADMIN")){
            throw new UnauthorizedException("You are not authorized to delete this extension.");
        }

        extensionRepository.deleteById(extensionId);
        extensionRepository.flush();
        extension.setFeatured(false);

        updateFeatured(extension);
        updateMostRecent();

        return extension;
    }

    @Override
    public List<Extension> findMostRecent(Integer mostRecentCount){
        mostRecentCount = mostRecentCount == null ?
                mostRecentQueueLimit : mostRecentCount;

        if(mostRecentCount > mostRecentQueueLimit){
            return extensionRepository.findAllByUploadDate(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59), "", 0, PageRequest.of(0, mostRecentCount)).getContent();
        }

        return mostRecent.subList(0, Math.min(mostRecent.size(), mostRecentCount));
    }

    @Override
    public List<Extension> findFeatured(){
        return featured.values().stream().toList();
    }

    @Override
    public Page<Extension> findAllByDownloaded(int lastDownloadCount, int pageSize, String name, long lastId){
        return extensionRepository.findAllByDownloaded(lastDownloadCount, name, lastId,
                PageRequest.of(0, pageSize));
    }

    @Override
    public Page<Extension> findAllByCommitDate(LocalDateTime lastDate, int pageSize, String name, long lastId){
        return extensionRepository.findAllByCommitDate(lastDate, name, lastId,
                PageRequest.of(0, pageSize));
    }

    @Override
    public Page<Extension> findAllByUploadDate(LocalDateTime lastDate, int pageSize, String name, long lastId){
        return extensionRepository.findAllByUploadDate(lastDate, name, lastId,
                PageRequest.of(0, pageSize));
    }

    @Override
    public Page<Extension> findAllByName(String lastName, int pageSize, String name){
        return extensionRepository.findAllByName(name, lastName,
                PageRequest.of(0, pageSize));
    }

    @Override
    public Extension setPending(long extensionId, boolean state) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));

        if(state){
            featured.remove(extensionId);
            extension.setFeatured(false);
        }

        extension.setPending(state);
        extensionRepository.save(extension);
        updateMostRecent();

        return extension;
    }

    @Override
    public Extension setFeatured(long extensionId, boolean state) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));

        if (!extension.isFeatured() && state && featured.size() == featuredLimit) {
            throw new FeaturedLimitException(String.format("Only %s extensions can be featured. To free space first un-feature another extension.", featuredLimit));
        }

        extension.setFeatured(state);
        extensionRepository.save(extension);
        updateFeatured(extension);

        return extension;
    }

    @Override
    public Page<Extension> findUserExtensions(int pageSize, long lastId, UserModel user) {
        return extensionRepository.findUserExtensions(user, lastId,
                PageRequest.of(0, pageSize, Sort.Direction.ASC, "id"));
    }

    @Override
    public Page<Extension> findByPending(boolean state, int pageSize, long lastId) {
        return extensionRepository.findByPending(state, lastId,
                PageRequest.of(0, pageSize, Sort.Direction.ASC, "id"));
    }

    @Override
    public void loadFeatured() {
        extensionRepository.findByFeatured(true).forEach(extension ->
                featured.put(extension.getId(), extension));
    }

    @Override
    public void updateMostRecent(){
        mostRecent.clear();
        mostRecent.addAll(extensionRepository.findAllByUploadDate(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59), "", 0, PageRequest.of(0, mostRecentQueueLimit)).getContent());
    }

    public void updateFeatured(Extension extension) {
        if(extension.isFeatured()){
            featured.put(extension.getId(), extension);
        }else{
            featured.remove(extension.getId());
        }
        reloadExtension(extension);
    }

    @Override
    public Extension reloadExtension(Extension extension){
        if(featured.containsKey(extension.getId())){
            featured.replace(extension.getId(), extension);
        }

        int index = mostRecent.indexOf(extension);
        if(index != -1){
            mostRecent.set(index, extension);
        }
        return extension;
    }

    @Override
    public void reloadFile(File file){
        featured.forEach((integer, extension) -> {
            if(extension.getFile() != null && extension.getFile().equals(file))
                extension.getFile().setDownloadCount(file.getDownloadCount());
        });

        mostRecent.forEach(extension -> {
            if (extension.getFile() != null && extension.getFile().equals(file))
                extension.getFile().setDownloadCount(file.getDownloadCount());
        });
    }

    @Override
    public Page<Extension> findByTag(String name, int pageSize, long lastId) {
        return extensionRepository.findByTag(name, lastId, PageRequest.of(0, pageSize, Sort.Direction.ASC, "id"));
    }

    @Override
    public Extension getById(long id) {
        return extensionRepository.getById(id);
    }

    @Override
    public boolean isNameAvailable(String name){
        return extensionRepository.findByName(name) == null;
    }

    @Override
    public int getFeaturedLimit() {
        return featuredLimit;
    }

    @Override
    public void setFeaturedLimit(int limit) {
        featuredLimit = limit;
    }

    public int getMostRecentQueueLimit(){
        return mostRecentQueueLimit;
    }
}

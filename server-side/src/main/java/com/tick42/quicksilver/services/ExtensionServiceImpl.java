package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.PageDto;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.services.base.ExtensionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
public class ExtensionServiceImpl implements ExtensionService {
    private final ExtensionRepository extensionRepository;
    private Map<Long, Extension> featured = Collections.synchronizedMap(new LinkedHashMap<>());
    private List<Extension> mostRecent = Collections.synchronizedList(new ArrayList<>());
    private int mostRecentQueueLimit = 5;
    private int featuredLimit = 4;

    public ExtensionServiceImpl(ExtensionRepository extensionRepository) {
        this.extensionRepository = extensionRepository;
    }

    @Override
    public Extension findById(long extensionId, UserDetails loggedUser) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));

        UserModel owner = extension.getOwner();
        if(extension.getIsPending() && (loggedUser == null || (!AuthorityUtils.authorityListToSet(loggedUser.getAuthorities()).contains("ROLE_ADMIN") &&
                (loggedUser.getId() != owner.getId() || owner.getIsActive())))){
            throw new EntityUnavailableException("Extension is unavailable.");
        }

        return extension;
    }

    @Override
    public Extension update(Extension newExtension) {
        Extension extension = extensionRepository.findById(newExtension.getId())
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));

        if (newExtension.getOwner().getId() != extension.getOwner().getId() && !newExtension.getOwner().getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedException("You are not authorized to edit this extension.");
        }

        return extensionRepository.save(newExtension);
    }

    @Override
    public Extension save(Extension extension){
        return extensionRepository.save(extension);
    }

    @Override
    public Extension delete(long extensionId, UserDetails loggedUser) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));

        UserModel owner = extension.getOwner();
        if(!AuthorityUtils.authorityListToSet(loggedUser.getAuthorities()).contains("ROLE_ADMIN") &&
                loggedUser.getId() != owner.getId() && owner.getIsActive()){
            throw new EntityUnavailableException("You are not authorized to delete this extension.");
        }
        extensionRepository.delete(extension);

        return extension;
    }

    @Override
    public List<Extension> findMostRecent(Integer mostRecentCount){
        List<Extension> mostRecentExtensions;
        if(mostRecentCount == null){
            mostRecentExtensions = new ArrayList<>(mostRecent);
        }else if(mostRecentCount > mostRecentQueueLimit){
            mostRecentExtensions = extensionRepository.findAllOrderedBy("",PageRequest.of(0, mostRecentCount, Sort.Direction.DESC, "uploadDate"));
        }else{
            mostRecentExtensions = new ArrayList<>(mostRecent).subList(0, mostRecentCount);
        }
        return mostRecentExtensions;
    }

    @Override
    public List<Extension> findFeatured(){
        return new ArrayList<>(featured.values());
    }

    @Override
    public List<Extension> findMostDownloaded(Integer mostDownloadedCount){
        return extensionRepository.findAllOrderedBy("", PageRequest.of(0, mostDownloadedCount, Sort.Direction.DESC, "timesDownloaded"));
    }

    @Override
    public long findTotalResults(String name){
        return extensionRepository.getTotalResults(name);
    }

    @Override
    public PageDto<Extension> findPageWithCriteria(String name, String orderBy, Integer page, Integer pageSize) {

        if (page == null || page < 0) {
            page = 0;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        if (name == null) {
            name = "";
        }

        if (orderBy == null) {
            orderBy = "date";
        }

        long totalResults = findTotalResults(name);
        int totalPages = (int) Math.ceil(totalResults * 1.0 / pageSize);

        if (page > totalPages && totalResults != 0) {
            throw new InvalidParameterException("Page" + totalPages + " is the last page. Page " + page + " is invalid.");
        }

        List<Extension> extensions;
        switch (orderBy) {
            case "date":
                extensions = extensionRepository.findAllOrderedBy(name,PageRequest.of(page, pageSize, Sort.Direction.DESC, "uploadDate"));
                break;
            case "commits":
                extensions = extensionRepository.findAllOrderedBy(name,PageRequest.of(page, pageSize, Sort.Direction.DESC, "github.lastCommit"));
                break;
            case "name":
                extensions = extensionRepository.findAllOrderedBy(name, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
                break;
            case "downloads":
                extensions = extensionRepository.findAllOrderedBy(name, PageRequest.of(page, pageSize, Sort.Direction.DESC, "timesDownloaded"));
                break;
            default:
                throw new InvalidParameterException("\"" + orderBy + "\" is not a valid parameter. Use \"date\", \"commits\", \"name\" or \"downloads\".");
        }

        return new PageDto<>(extensions, page, totalPages, totalResults);
    }

    @Override
    public Extension setPublishedState(long extensionId, String state) {

        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));

        switch (state) {
            case "publish":
                extension.setIsPending(false);
                break;
            case "unpublish":
                featured.remove(extensionId);
                extension.isFeatured(false);
                extension.setIsPending(true);
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid extension state. Use \"publish\" or \"unpublish\".");
        }

        extensionRepository.save(extension);
        updateMostRecent();
        return extension;
    }

    @Override
    public Extension setFeaturedState(long extensionId, String state) {

        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new EntityNotFoundException("Extension not found."));


        switch (state) {
            case "feature":
                if(!extension.isFeatured() && featured.size() == featuredLimit){
                    throw new FeaturedLimitException(String.format("Only %s extensions can be featured. To free space first un-feature another extension.", featuredLimit));
                }
                extension.isFeatured(true);
                break;
            case "unfeature":
                extension.isFeatured(false);
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid featured state. Use \"feature\" or \"unfeature\".");
        }

        if(extension.isFeatured()){
            featured.put(extension.getId(), extension);
        }else{
            featured.remove(extensionId);
        }
        reloadExtension(extension);

        return extension;
    }

    @Override
    public List<Extension> findPending() {
        return extensionRepository.findByPending(true);
    }

    @Override
    public void loadFeatured() {
        extensionRepository.findByFeatured(true).forEach(extension ->
                featured.put(extension.getId(), extension));
    }

    @Override
    public void updateMostRecent(){
        mostRecent.clear();
        mostRecent.addAll(extensionRepository.findAllOrderedBy("",PageRequest.of(0, mostRecentQueueLimit, Sort.Direction.DESC, "uploadDate")));
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
            if(extension.getFile().equals(file))
                extension.getFile().setDownloadCount(file.getDownloadCount());
        });

        mostRecent.forEach(extension -> {
            if (extension.getFile() == file)
                extension.getFile().setDownloadCount(file.getDownloadCount());
        });
    }

    @Override
    public boolean checkName(String name){
        return extensionRepository.findByName(name) == null;
    }
}

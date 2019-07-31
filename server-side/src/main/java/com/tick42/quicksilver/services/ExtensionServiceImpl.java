package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.DTO.PageDTO;
import com.tick42.quicksilver.models.Spec.ExtensionSpec;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.services.base.ExtensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExtensionServiceImpl implements ExtensionService {

    private final ExtensionRepository extensionRepository;
    private Map<Integer, Extension> featured = Collections.synchronizedMap(new LinkedHashMap<>());
    private List<Extension> mostRecent = Collections.synchronizedList(new ArrayList<>());
    private int mostRecentQueueLimit = 5;
    private int featuredLimit = 4;

    @Autowired
    public ExtensionServiceImpl(ExtensionRepository extensionRepository) {
        this.extensionRepository = extensionRepository;
    }

    @Override
    public Extension findById(int extensionId, UserDetails loggedUser) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));

        checkUserAndExtension(extension, loggedUser);

        return extension;
    }

    @Override
    public Extension update(ExtensionSpec extensionSpec, UserModel user, Set<Tag> tags) {

        Extension extension = extensionRepository.findById(extensionSpec.getId())
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));

        if (user.getId() != extension.getOwner().getId() && !user.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedExtensionModificationException("You are not authorized to edit this extension.");
        }

        extension.setName(extensionSpec.getName());
        extension.setVersion(extensionSpec.getVersion());
        extension.setDescription(extensionSpec.getDescription());
        extension.setTags(tags);

        return extensionRepository.save(extension);
    }

    @Override
    public Extension save(Extension extension){
        return extensionRepository.save(extension);
    }

    @Override
    public void delete(int extensionId, UserModel user) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));


        if (user.getId() != extension.getOwner().getId() && !user.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedExtensionModificationException("You are not authorized to delete this extension.");
        }

        extensionRepository.delete(extension);
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
    public List<Extension> getFeatured(){
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
    public PageDTO<Extension> findPageWithCriteria(String name, String orderBy, Integer page, Integer pageSize) {

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

        return new PageDTO<>(extensions, page, totalPages, totalResults);
    }

    @Override
    public Extension setPublishedState(int extensionId, String state) {

        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));

        switch (state) {
            case "publish":
                extension.isPending(false);
                break;
            case "unpublish":
                extension.isPending(true);
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid extension state. Use \"publish\" or \"unpublish\".");
        }

        extensionRepository.save(extension);
        updateMostRecent();
        return extension;
    }

    @Override
    public Extension setFeaturedState(int extensionId, String state) {

        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));


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

        return extension;
    }

    @Override
    public List<Extension> findPending() {
        return extensionRepository.findByPending(true);
    }

    private void checkUserAndExtension(Extension extension, UserDetails loggedUser) {
        if (extension == null) {
            throw new ExtensionNotFoundException("Extension doesn't exist.");
        }
        boolean admin = false;
        if(loggedUser != null){
            Set<String> authorities = AuthorityUtils.authorityListToSet(loggedUser.getAuthorities());
            admin = authorities.contains("ROLE_ADMIN");
        }

        if (!extension.getOwner().getIsActive() &&
                ((loggedUser == null) || (!admin))) {
            throw new ExtensionUnavailableException("Extension is unavailable.");
        }

        if (extension.isPending() &&
                ((loggedUser == null) ||
                        (extension.getOwner().getId() != loggedUser.getId() && !admin))) {
            throw new ExtensionUnavailableException("Extension is unavailable.");
        }
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
        return extension;
    }
    @Override
    public boolean checkName(String name){
        return extensionRepository.findByName(name) == null;
    }
}

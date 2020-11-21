package com.tick42.quicksilver.models.Dtos;

import java.util.List;

public class PageDto<T> {
    private int currentPage;
    private int totalPages;
    private Long totalResults;
    private List<T> extensions;

    public PageDto() {

    }

    public PageDto(List<T> extensions, int currentPage, int totalPages, Long totalResults) {
        this.currentPage = currentPage;
        this.totalResults = totalResults;
        this.extensions = extensions;
        this.totalPages = totalPages;
    }
    public PageDto(PageDto pageDto) {
        this.currentPage = pageDto.getCurrentPage();
        this.totalResults = pageDto.getTotalResults();
        this.totalPages = pageDto.getTotalPages();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public Long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Long totalResults) {
        this.totalResults = totalResults;
    }

    public List<T> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<T> extensions) {
        this.extensions = extensions;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

package com.tick42.quicksilver.models.Dtos;

import java.util.List;

public class PageDto<T> {
    private int currentPage;
    private int totalPages;
    private Long totalResults;
    private List<T> data;

    public PageDto() {

    }

    public PageDto(List<T> data, int currentPage, int totalPages, Long totalResults) {
        this.currentPage = currentPage;
        this.totalResults = totalResults;
        this.data = data;
        this.totalPages = totalPages;
    }
    public PageDto(PageDto pageDto) {
        this.currentPage = pageDto.getCurrentPage();
        this.totalResults = pageDto.getTotalResults();
        this.totalPages = pageDto.getTotalPages();
    }

    public PageDto(long totalResults, List<T> data){
        this.data = data;
        this.totalResults = totalResults;
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

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}

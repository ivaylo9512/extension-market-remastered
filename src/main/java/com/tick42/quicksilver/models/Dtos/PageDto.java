package com.tick42.quicksilver.models.Dtos;

import java.util.List;

public class PageDto<T> {
    private int totalPages;
    private Long totalResults;
    private List<T> data;

    public PageDto() {

    }

    public PageDto(List<T> data, int totalPages, Long totalResults) {
        this.totalResults = totalResults;
        this.data = data;
        this.totalPages = totalPages;
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

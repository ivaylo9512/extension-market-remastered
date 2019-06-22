package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.GitHubModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubRepository extends JpaRepository<GitHubModel, Integer> {

}

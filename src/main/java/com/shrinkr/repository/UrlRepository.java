package com.shrinkr.repository;

import com.shrinkr.entity.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long>, PagingAndSortingRepository<Url, Long> {
    Page<Url> findByUserId(Long userId, Pageable pageable);
    Boolean existsByShortId(String shortId);
    Url findByShortId(String shortId);
}
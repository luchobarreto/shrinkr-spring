package com.shrinkr.service;

import com.shrinkr.entity.Url;
import com.shrinkr.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

public interface UrlService {

    Page<Url> getUrls(Long userId, int page, int size);
    URI createUrl(User user, Url url);

    Url getUrlById(Long id);
    Url getUrlByShortId(String id) throws ResponseStatusException;
    void updateUrl(Long id, Url urlDetails, Long userId);
    void deleteUrl(Url url, Long userId);
}

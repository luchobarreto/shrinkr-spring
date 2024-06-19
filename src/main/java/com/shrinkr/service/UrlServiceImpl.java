package com.shrinkr.service;

import com.shrinkr.entity.Url;
import com.shrinkr.entity.User;
import com.shrinkr.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Random;

@Service
public class UrlServiceImpl implements UrlService {

    private static final String URL = "/users";
    private final UrlRepository urlRepository;

    @Autowired
    UrlServiceImpl(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @PreAuthorize("#userId == principal.id")
    public Page<Url> getUrls(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return urlRepository.findByUserId(userId, pageable);
    }

    public URI createUrl(User user, Url url) {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final int shortIdLength = 5;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < shortIdLength; i++) {
            int idx = random.nextInt(characters.length());
            sb.append(characters.charAt(idx));
        }

        String shortId = sb.toString();

        while(urlRepository.existsByShortId(shortId)) {
            for(int i = 0; i < shortIdLength; i++) {
                int idx = random.nextInt(characters.length());
                sb.setCharAt(i, characters.charAt(idx));
            }
            shortId = sb.toString();
        };

        url.setShortId(sb.toString());
        url.setUser(user);
        Url newUrl = urlRepository.save(url);

        return ServletUriComponentsBuilder
                .fromCurrentContextPath().path(URL + "/{id}")
                .buildAndExpand(newUrl.getId()).toUri();
    }

    public Url getUrlByShortId(String shortId) throws ResponseStatusException {
        Url url = urlRepository.findByShortId(shortId);
        if(url == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Url with id: " + shortId + " does not exists.");
        }
        url.setViews(url.getViews() + 1);
        urlRepository.save(url);
        return url;
    }

    public Url getUrlById(Long id) {
        return urlRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Url with id: " + id + " does not exists."));
    }

    @PreAuthorize("#userId == #urlDetails.user.id")
    public void updateUrl(Long id, Url urlDetails, Long userId) {
        Url url = getUrlById(id);
        url.setUrl(urlDetails.getUrl());

        urlRepository.save(url);
    }

    @PreAuthorize("#userId == #url.user.id")
    public void deleteUrl(Url url, Long userId) {
        urlRepository.delete(url);
    }
}

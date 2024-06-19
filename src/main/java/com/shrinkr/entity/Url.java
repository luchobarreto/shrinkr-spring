package com.shrinkr.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.URL;

import java.util.Date;

@Entity
@Table(name = "urls")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Url is mandatory")
    @URL(message = "Invalid Url")
    @Column(nullable = false)
    private String url;

    @Column(name = "short_id")
    private String shortId;

    @Column(columnDefinition = "INT DEFAULT 0 NOT NULL")
    private int views;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getShortId() {
        return shortId;
    }

    public int getViews() {
        return views;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setViews(int views) {
        this.views = views;
    }
}

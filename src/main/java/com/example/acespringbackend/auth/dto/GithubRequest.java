package com.example.acespringbackend.auth.dto;

public class GithubRequest {
    private String code;

    public void GithubRequest() {}

    public void GithubRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

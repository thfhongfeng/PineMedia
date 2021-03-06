package com.pine.tool.request;

/**
 * Created by tanghongfeng on 2018/9/16
 */

public enum RequestMethod {
    GET("GET"),

    POST("POST"),

    PUT("PUT"),

    DELETE("DELETE"),

    HEAD("HEAD"),

    PATCH("PATCH"),

    OPTIONS("OPTIONS"),

    TRACE("TRACE");

    private final String value;

    RequestMethod(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public boolean allowRequestBody() {
        switch (this) {
            case POST:
            case PUT:
            case PATCH:
            case DELETE:
                return true;
            default:
                return false;
        }
    }
}

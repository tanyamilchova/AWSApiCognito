package com.task12.handlers;
//
import java.util.Objects;


public class RouteKey {
    private final String method;
    private final String path;

    public RouteKey(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RouteKey routeKey = (RouteKey) obj;
        return method.equals(routeKey.method) && path.equals(routeKey.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }

}
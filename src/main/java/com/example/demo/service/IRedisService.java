package com.example.demo.service;

public interface IRedisService {
    Boolean setnx(String key, Object val);
    Object getset(String key, Object val);
    Boolean del(String key);
    String get(String key);
    void set(String key, Object val);
    <T> T get(String key, Class<T> cls);
}

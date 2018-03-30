package com.example.demo.controller;

import com.example.demo.service.IRedisService;
import com.example.demo.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class RedisController {
    private final static Logger logger = LoggerFactory.getLogger(RedisController.class);
    @Resource
    private IRedisService iRedisService;
    @Resource
    private SyncService syncService;

    @GetMapping("/get")
    @ResponseBody
    public String test() {
        return iRedisService.get("hello");
    }

    @GetMapping("/set")
    @ResponseBody
    public String set() {
        iRedisService.set("hello", "world");
        return "Set Ok!";
    }

    @GetMapping("/sync")
    @ResponseBody
    public String sync() {
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                System.out.println(syncService.getIndex());
            }
        }).start();
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                System.out.println(syncService.getIndex());
            }
        }).start();
        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                System.out.println(syncService.getIndex());
            }
        }).start();
        return "SUCCESS";
    }
}

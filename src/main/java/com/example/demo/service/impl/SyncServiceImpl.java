package com.example.demo.service.impl;

import com.example.demo.annotions.RedisSync;
import com.example.demo.service.SyncService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SyncServiceImpl implements SyncService {
    private static int COUNT = 100;
    private static final Random RANDOM = new Random();
    @Override
    @RedisSync
    public int getIndex() {
        try {
            //随机获取等待时间（该时间仅供参考，准确时间还需加上代码执行时间）
            long time = 500+RANDOM.nextInt(2500);
            System.out.println("COUNT("+COUNT+")，sleep:"+time);
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (COUNT<=0) {
            return 0;
        }
        return COUNT--;
    }
}

package com.example.demo.aop;

import com.example.demo.annotions.RedisSync;
import com.example.demo.service.IRedisService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
public class RedisSyncAop {
    private static final Logger logger = LoggerFactory.getLogger(RedisSyncAop.class);
    @Resource
    private IRedisService iRedisService;

    @Pointcut("@annotation(com.example.demo.annotions.RedisSync)")
    private void anyMethod(){
    }

    @Around("anyMethod()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        //获得锁
        Method method = ((MethodSignature)pjp.getSignature()).getMethod();
        String key = method.toString();
        RedisSync redisSync = method.getAnnotation(RedisSync.class);
        //解决代理模式下无法获取真实的方法，导致无法获取方法上的注解问题
        if (redisSync==null) {
            Method realMethod = pjp.getTarget().getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            redisSync = realMethod.getAnnotation(RedisSync.class);
        }
        long waitTime = redisSync.waitTime();
        long currTime = System.currentTimeMillis();
        Boolean state = iRedisService.setnx(key, currTime);
        long saveTime = 0L;
        while (!state) {
            Long tempSaveTime = iRedisService.get(key, Long.class);
            //锁被释放
            if (tempSaveTime==null) {
                state = iRedisService.setnx(key, currTime);
                continue;
            }
            //锁被重新获取
            if (!tempSaveTime.equals(saveTime)){
                currTime = System.currentTimeMillis();
                saveTime=tempSaveTime;
            }
            //判断是否超时
            if (saveTime+redisSync.timeout()<currTime) {
                //超时，直接获得锁
                Object tempTime = iRedisService.getset(key, currTime);
                if(tempTime==null){
                    state = iRedisService.setnx(key, currTime);
                    continue;
                }
                //判断锁被是否被释放或未被抢先获取
                if (Objects.equals(saveTime, tempTime)) {
                    logger.warn("方法：{}，执行超时，已被强制解锁！", key);
                    break;
                }
            }
            //等待
            if(waitTime>0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            state = iRedisService.setnx(key, currTime);
        }
        try{
            //执行
            result = pjp.proceed();
        } finally {
            Long currSaveTime = iRedisService.get(key, Long.class);
            //判断锁未被判定为超时
            if (currSaveTime!=null && Objects.equals(currSaveTime, currTime)) {
                //释放锁
                iRedisService.del(key);
            }
        }
        return result;
    }
}

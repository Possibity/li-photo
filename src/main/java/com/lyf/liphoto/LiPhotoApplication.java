package com.lyf.liphoto;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.lyf.liphoto.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class LiPhotoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiPhotoApplication.class, args);
    }

}

package cn.net.colin.geotoolsdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class GeotoolsDemoApplication /*extends SpringBootServletInitializer */{
    /*@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GeotoolsDemoApplication.class);
    }*/
    public static void main(String[] args) {
        SpringApplication.run(GeotoolsDemoApplication.class, args);
    }
}

package org.dromara.datacenter;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * Data center module.
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiDataCenterApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiDataCenterApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("Data-Center module started successfully.");
    }
}

package dev.ioexception.rarebid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RarebidApplication {

    public static void main(String[] args) {
        SpringApplication.run(RarebidApplication.class, args);
    }
}

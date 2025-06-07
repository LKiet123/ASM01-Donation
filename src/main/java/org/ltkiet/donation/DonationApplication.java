package org.ltkiet.donation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.ltkiet.donation", "org.ltkiet.donation.service"})
public class DonationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DonationApplication.class, args);
    }

}

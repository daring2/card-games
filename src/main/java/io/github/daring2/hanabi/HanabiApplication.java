package io.github.daring2.hanabi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration;

@SpringBootApplication
@ConfigurationPropertiesScan
@Import(TelegramBotStarterConfiguration.class)
public class HanabiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HanabiApplication.class, args);
    }

}

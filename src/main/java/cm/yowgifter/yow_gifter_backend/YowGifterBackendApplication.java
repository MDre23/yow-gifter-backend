package cm.yowgifter.yow_gifter_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YowGifterBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(YowGifterBackendApplication.class, args);
	}

}

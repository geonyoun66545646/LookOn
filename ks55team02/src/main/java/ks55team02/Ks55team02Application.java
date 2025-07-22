package ks55team02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAspectJAutoProxy
public class Ks55team02Application {

	public static void main(String[] args) {
		SpringApplication.run(Ks55team02Application.class, args);
	}
	

}

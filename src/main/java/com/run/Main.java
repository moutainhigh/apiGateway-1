package com.run;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

@SpringBootApplication
@Configuration
@EnableApolloConfig
@EnableZuulProxy
@ImportResource("classpath:config/spring-context.xml")
public class Main {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

}

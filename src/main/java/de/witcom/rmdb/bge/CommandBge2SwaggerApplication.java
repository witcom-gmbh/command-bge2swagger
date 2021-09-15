package de.witcom.rmdb.bge;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.witcom.rmdb.bge.generator.SwaggerGenerator;

@SpringBootApplication
public class CommandBge2SwaggerApplication {
	


	public static void main(String[] args) {
		SpringApplication.run(CommandBge2SwaggerApplication.class, args);
		
		
	}


}

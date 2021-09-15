package de.witcom.rmdb.bge.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class GeneratorConfig {
	
	@Bean
	@Qualifier("entitiesToGenerate")
	List<String> entitiesToGenerate(final Environment environment) {
	    final String entities = environment.getProperty("ENTITY_LIST");
	    if (entities == null) {
	    	return new ArrayList<String>();
	    }
	    return Arrays.asList(entities.split(","));
	}

}

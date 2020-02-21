package de.witcom.rmdb.bge.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "job.autorun", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GeneratorRunner implements CommandLineRunner {
	
	private final Logger log = LoggerFactory.getLogger(GeneratorRunner.class);
	@Inject
	SwaggerGenerator swaggerGen;
	
	@Autowired()
	@Qualifier("entitiesToGenerate")
	List<String> entityList;
	
	@Value("${app.generator.output}")
	protected String output;


	@Override
	public void run(String... arg0)  {
		// TODO Auto-generated method stub
		log.debug("Ready to run");
		
		try {
			swaggerGen.initSwaggerGenerator();
			swaggerGen.generateRestServices();
			swaggerGen.generateEntities(entityList);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			File resultFile = new File(output);
			mapper.writeValue(resultFile, swaggerGen.getSwaggerDef());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}

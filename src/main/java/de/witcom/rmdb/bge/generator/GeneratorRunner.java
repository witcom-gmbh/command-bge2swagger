package de.witcom.rmdb.bge.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeneratorRunner implements CommandLineRunner {
	
	private final Logger log = LoggerFactory.getLogger(GeneratorRunner.class);
	@Inject
	SwaggerGenerator swaggerGen;
	
	@Value("${app.generator.output-dir}")
	protected String outputDir;


	@Override
	public void run(String... arg0)  {
		// TODO Auto-generated method stub
		log.debug("Ready to run");
		
		try {
			swaggerGen.initSwaggerGenerator();
			ArrayList<String> filterList = new ArrayList<String>();
			filterList.add("easySearch");
			swaggerGen.generateEntities(filterList);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			File resultFile = new File(outputDir+"/swagger-bge-api-base.json");
			mapper.writeValue(resultFile, swaggerGen.getSwaggerDef());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			swaggerGen.initSwaggerGenerator();
			swaggerGen.generateRestServices();
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			File resultFile = new File(outputDir+"/swagger-rest-ci.json");
			mapper.writeValue(resultFile, swaggerGen.getSwaggerDef());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<String> filterList = new ArrayList<String>();
		
		filterList.add("campus");
		filterList.add("building");
		filterList.add("room");
		filterList.add("floor");
		filterList.add("zone");
		filterList.add("custom.ctcdynPostalAddress");
		//generateForEntities("zone",filterList);
		
		filterList = new ArrayList<String>();
		filterList.add("chassis");
		filterList.add("deviceall");
		filterList.add("masterdevice");
		filterList.add("module");
		filterList.add("passivemodule");
		filterList.add("switchcabinet");
		filterList.add("warehouse");
		//generateForEntities("base",filterList);

		filterList = new ArrayList<String>();
		filterList.add("organisation");
		filterList.add("person");
		//generateForEntities("acm",filterList);
		
		filterList = new ArrayList<String>();
		filterList.add("bearer");
		filterList.add("serviceTelcoPath");
		filterList.add("serviceTelcoUnroutedPath");
		filterList.add("serviceTelcoMultipoint");
		filterList.add("serviceTelcoPointToPoint");
		filterList.add("serviceTelcoUnroutedMultipoint");
		filterList.add("serviceTypeDefinition");
		generateForEntities("telco",filterList);
		
		
		
		
	}
	
	public void generateForEntities(String groupIdentifier,List<String> filterList) {
		
		try {
			swaggerGen.initSwaggerGenerator();
			swaggerGen.updateSwaggerInfo("Swagger for Command BGE - " + groupIdentifier , "Swagger API for FNT Command Business Gateway");
			
			swaggerGen.generateEntities(filterList);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			File resultFile = new File(outputDir+"/swagger-bge-"+groupIdentifier+".json");
			mapper.writeValue(resultFile, swaggerGen.getSwaggerDef());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}

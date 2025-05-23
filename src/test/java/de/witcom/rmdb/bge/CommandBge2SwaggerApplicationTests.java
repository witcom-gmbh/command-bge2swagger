package de.witcom.rmdb.bge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fntsoftware.businessgateway.internal.doc.dto.ComplexTypeDto;
import com.fntsoftware.businessgateway.internal.doc.dto.RestDto;
import com.fntsoftware.businessgateway.internal.doc.dto.SystemInfoDto;
import com.fntsoftware.businessgateway.internal.doc.dto.TypeDto;
import com.fntsoftware.businessgateway.internal.doc.dto.TypeDtoBase;

import de.witcom.rmdb.bge.generator.SwaggerGenerator;
import de.witcom.rmdb.bge.mixins.TypeDtoMixIn;


@RunWith(SpringRunner.class)
//@ActiveProfiles("test")
@SpringBootTest(properties = {"job.autorun.enabled=false"})
public class CommandBge2SwaggerApplicationTests {
	
	private final Logger log = LoggerFactory.getLogger(CommandBge2SwaggerApplicationTests.class);

	@Inject
	SwaggerGenerator swaggerGen;
	
	@Autowired()
	@Qualifier("entitiesToGenerate")
	List<String> entityList;
	
	@Test
	//@Ignore
	public void testGenerator() throws Exception {
		
		if (entityList.isEmpty()) {
		 	entityList.add("easysearch");
		}
		
		swaggerGen.generateEntities(entityList);
		swaggerGen.generateRestServices();
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		File resultFile = new File("/tmp/swagger-test-bge.json");
		
		mapper.writeValue(resultFile, swaggerGen.getSwaggerDef());

	}
	


}

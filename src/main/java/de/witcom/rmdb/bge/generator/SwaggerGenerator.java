package de.witcom.rmdb.bge.generator;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonAppend.Attr;
import com.fntsoftware.businessgateway.generation.webservice.data.ServiceStatusData;
import com.fntsoftware.businessgateway.internal.doc.dto.AttributeDto;
import com.fntsoftware.businessgateway.internal.doc.dto.EntityDto;
import com.fntsoftware.businessgateway.internal.doc.dto.EntityInfoDto;
import com.fntsoftware.businessgateway.internal.doc.dto.ListTypeDto;
import com.fntsoftware.businessgateway.internal.doc.dto.OperationDto;
import com.fntsoftware.businessgateway.internal.doc.dto.QueryDto;
import com.fntsoftware.businessgateway.internal.doc.dto.RelationDto;
import com.fntsoftware.businessgateway.internal.doc.dto.SubOperationDto;
import com.fntsoftware.businessgateway.internal.doc.dto.SystemInfoDto;
import com.fntsoftware.businessgateway.internal.doc.dto.TypeDto;

import de.witcom.rmdb.bge.Constants;
import de.witcom.rmdb.bge.mixins.TypeDtoMixIn;
import io.swagger.models.ArrayModel;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

@Service
public class SwaggerGenerator {

	private final Logger log = LoggerFactory.getLogger(SwaggerGenerator.class);

	private Swagger swaggerDef;

	protected ObjectMapper mapper;
	protected String swaggerHost;
	
	@Value("${app.command.base-url}")
	protected String bgeBaseUrl;

	public void generateEntities() throws Exception{
		this.generateEntities(new ArrayList<String>());
	}

	
	public void generateRestServices(){
		
		
		//Type Definitionen
		//CI-Attribute
		ModelImpl definition = new ModelImpl();
		definition.setType("object");
		Map<String, Property> props = new HashMap<String, Property>();
		props.put("keyLabel", new StringProperty());
		props.put("valueLabel", new StringProperty());
		props.put("key", new StringProperty());
		props.put("value", new StringProperty());
		definition.setProperties(props);
		definition.setTitle("CiAttribute");
		this.swaggerDef.getDefinitions().put("CiAttribute", definition);
		
		//CiAttributes
		//Array of CI-Attribute
		ArrayModel arrayDefinition = new ArrayModel();
		arrayDefinition.setItems(new RefProperty("#/definitions/CiAttribute"));
		arrayDefinition.setTitle("ciAttributes");
		this.swaggerDef.getDefinitions().put("ciAttributes", arrayDefinition);

		//Link-Attribute
		definition = new ModelImpl();
		definition.setType("object");
		props = new HashMap<String, Property>();
		props.put("keyLabel", new StringProperty());
		props.put("valueLabel", new StringProperty());
		props.put("key", new StringProperty());
		props.put("value", new StringProperty());
		definition.setProperties(props);
		definition.setTitle("LinkAttribute");
		this.swaggerDef.getDefinitions().put("LinkAttribute", definition);
		
		//LinkAttributes
		//Array of Link-Attribute
		arrayDefinition = new ArrayModel();
		arrayDefinition.setItems(new RefProperty("#/definitions/LinkAttribute"));
		arrayDefinition.setTitle("linkAttributes");
		this.swaggerDef.getDefinitions().put("linkAttributes", arrayDefinition);		
		
		//status
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("status");
		props = new HashMap<String, Property>();
		props.put("success", new BooleanProperty());
		props.put("localizedMessage", new StringProperty());
		props.put("message", new StringProperty());
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("status", definition);
		
		//entity
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("entity");
		props = new HashMap<String, Property>();
		props.put("translatedValue", new StringProperty());
		props.put("value", new StringProperty());
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("entity", definition);
		
		//ci
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("ci");
		props = new HashMap<String, Property>();		
		props.put("id", new StringProperty());
		props.put("elid", new StringProperty());
		props.put("iconName", new StringProperty());
		props.put("displayString", new StringProperty());
		props.put("entity", new RefProperty("#/definitions/entity"));
		props.put("typePlate", new RefProperty("#/definitions/ciAttributes"));
		props.put("typeElid", new StringProperty());
		props.put("description", new StringProperty());
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("ci", definition);
		
		//Linked-CI-Object
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("linkedCi");
		props = new HashMap<String, Property>();
		props.put("linkAttributes", new RefProperty("#/definitions/linkAttributes"));
		props.put("ci", new RefProperty("#/definitions/ci"));
		props.put("linkElid", new StringProperty());
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("linkedCi", definition);		
		
		//Linked-CI-Object List
		arrayDefinition = new ArrayModel();
		arrayDefinition.setItems(new RefProperty("#/definitions/linkedCi"));
		arrayDefinition.setTitle("linkedCis");
		this.swaggerDef.getDefinitions().put("linkedCis", arrayDefinition);		
		
		
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("linkedCisResponse");
		props = new HashMap<String, Property>();
		props.put("linkedCis", new RefProperty("#/definitions/linkedCis"));
		props.put("status", new RefProperty("#/definitions/status"));
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("linkedCisResponse", definition);

		
		//props.put("status", new RefProperty("#/definitions/status"));
		
		//HeadData Response-Object
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("ciHeadDataResponse");
		props = new HashMap<String, Property>();
		props.put("ci", new RefProperty("#/definitions/ci"));
		props.put("status", new RefProperty("#/definitions/status"));
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("CiHeadDataResponse", definition);

		//Data Response-Object
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("ciDataResponse");
		props = new HashMap<String, Property>();
		props.put("attributes", new RefProperty("#/definitions/ciAttributes"));
		props.put("status", new RefProperty("#/definitions/status"));
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("CiDataResponse", definition);		
		
		//Operations
		//HeadData
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		params.add(new QueryParameter().name("sessionid").required(true).type("string").description("Session-ID"));
		params.add(new QueryParameter().name("planView").required(false).type("boolean").description("plan view"));
		params.add(new PathParameter().name("ciElid").required(true).type("string"));
		//Path path = new Path().get(getCIRESTOperation("CiHeadData",params));
		this.swaggerDef.getPaths().put("/rest/ci/{ciElid}/HeadData", new Path().get(getCIRESTOperation("CiHeadData",params)));
		
		//Data
		params = new ArrayList<Parameter>();
		params.add(new QueryParameter().name("sessionid").required(true).type("string").description("Session-ID"));
		params.add(new QueryParameter().name("baseAttributes").required(false).type("boolean"));
		params.add(new QueryParameter().name("extendedAttributes").required(false).type("boolean"));
		params.add(new QueryParameter().name("systemAttributes").required(false).type("boolean"));
		params.add(new QueryParameter().name("planView").required(false).type("boolean").description("plan view"));
		params.add(new PathParameter().name("ciElid").required(true).type("string"));
		this.swaggerDef.getPaths().put("/rest/ci/{ciElid}/Data", new Path().get(getCIRESTOperation("CiData",params)));
		
		params = new ArrayList<Parameter>();
		params.add(new QueryParameter().name("sessionid").required(true).type("string").description("Session-ID"));
		params.add(new QueryParameter().name("planView").required(false).type("boolean").description("plan view"));
		params.add(new PathParameter().name("ciElid").required(true).type("string"));
		//Path path = new Path().get(getCIRESTOperation("CiHeadData",params));
		this.swaggerDef.getPaths().put("/rest/ci/{ciElid}/LinkedCis", new Path().get(getCIRESTOperation("linkedCis",params)));
		
	}
	
	public Operation getCIRESTOperation(String opRequest,ArrayList<Parameter> params){
		//Operation
		Operation op = new Operation();
		op.setOperationId("get"+opRequest);
		List<String> tags = new ArrayList<String>();
		tags.add("CI");
		op.setTags(tags);
		List<String> contentTypes = new ArrayList<String>();
		contentTypes.add("application/json");
		op.setConsumes(contentTypes);
		op.setProduces(contentTypes);
		
		//parameters
		op.setParameters(params);
		Response response = new Response();
		response.setDescription("returns "+opRequest+"Response");
		response.setSchema(new RefProperty("#/definitions/"+opRequest+"Response"));
		op.addResponse("200", response);
		
		return op;
	}

	public void generateEntities(List<String> filter) throws Exception{

		//alle Entities

		
		JsonNode definition = restCall(getBGEDefUri());
		//ObjectMapper mapper = new ObjectMapper();
		//mapper.addMixIn(TypeDto.class, TypeDtoMixIn.class);
		SystemInfoDto dto = mapper.readValue(definition.toString(), SystemInfoDto.class);
		/*
		log.debug(dto.getVersion());

		Info info = new Info();
		info.setTitle("Swagger for Command BGE");
		info.setDescription("Swagger API for FNT Command Business Gateway");
		info.setVersion(dto.getVersion());

		swaggerDef.setInfo(info );
		swaggerDef.setHost(swaggerHost);
		swaggerDef.setBasePath("/axis/api/rest");

		List<Scheme> schemes = new ArrayList<Scheme>();
		schemes.add(Scheme.HTTPS);
		swaggerDef.setSchemes(schemes);
		*/

		List<EntityInfoDto> entities;

		if (filter.isEmpty()){
			entities = dto.getEntities();
			//entities = new ArrayList<EntityInfoDto>();
		} else {
			entities = dto.getEntities().stream().filter(entity -> {
				if (filter.contains(entity.getId()))
					return true;
				return false;
			}).collect(Collectors.toList());
		}

		entities.forEach(e -> {
			log.debug(e.getId());
			this.processEntity(e);
		});


	}

	private void processEntity(EntityInfoDto entity){
		JsonNode definition;
		//String entityId = getEntityId(entity);
		try {
			definition = restCall(getEntityDefUri(entity));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Error getting Entity-Definition - {}",e.toString());
			return;
		}

		//ObjectMapper mapper = new ObjectMapper();
		//mapper.addMixIn(TypeDto.class, TypeDtoMixIn.class);
		EntityDto entityDef;
		try {
			entityDef = mapper.readValue(definition.toString(),EntityDto.class);

			createTypeDefinition(getEntityId(entity)+"Data",entityDef.getAttributes());

			
			if (entityDef.getOperations() != null){
				for (OperationDto operation: entityDef.getOperations())
				{
					createBGEOperation(entityDef,operation);
				}
			}
			
			if (entityDef.getRelations() != null){
				for(RelationDto relation:entityDef.getRelations()){
					createRelation(entityDef,relation);
					
				}
			}
			
			if (entityDef.getQueries() != null){
				for (QueryDto query:entityDef.getQueries()){
					createQuery(entityDef,query);
					
				}
			}


			//Operations


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Error mapping Entity-Definition - {}",e.toString());
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		//entityDef.


		//EntityDto

	}

	private String getEntityId(EntityInfoDto entity){

		if (entity.getCustom()!=null){
			return entity.getId().substring(Constants.customPrefix.length());
		}

		return entity.getId();
	}

	private String getEntityId(EntityDto entity){

		if (entity.getCustom()!=null){
			return entity.getId().substring(Constants.customPrefix.length());
		}

		return entity.getId();
	}	

	private void createBGEOperation(EntityDto entity,OperationDto operation) throws Exception{
		//attribute

		String operationBaseName = StringUtils.capitalize(operation.getId()) + StringUtils.capitalize(getEntityId(entity));
		String pathToRemove = bgeBaseUrl + swaggerDef.getBasePath();
		String opPath = operation.getRest().getService().getUrl().substring(pathToRemove.length());
		List<String> tags = new ArrayList<String>();
		tags.add(entity.getId());
		log.debug("Lege Operation {} an",operationBaseName);
		Operation op = createBGEPostOperation(opPath, operation, operationBaseName, tags);
		Path path = new Path();
		path.setPost(op);
		this.swaggerDef.getPaths().put(opPath, path);

	}
	
	private void createQuery(EntityDto entity,QueryDto query){
		
		String queryBaseName = StringUtils.capitalize(getEntityId(entity))+StringUtils.capitalize(query.getId());
		log.debug("Lege Query {} an",queryBaseName);
		try {
			//Request Typ definieren
			//createRestrictionTypeDefinition(queryBaseName +Constants.suffixRequestData,query.getAttributes());
		
			//ResponseTyp definieren
			createTypeDefinition(queryBaseName +Constants.suffixResponseData,query.getAttributes()
				.stream()
				.filter(attr -> attr.getReturnable())
				.collect(Collectors.toList()));
			
			//Type-Definition fuer Entity-Restriction
			createRestrictionTypeDefinition(queryBaseName+"Restriction",query.getAttributes()
					.stream()
					.filter(attr -> attr.getRestrictable())
					.collect(Collectors.toList()));
			//Type-Definition fuer Return Entity-Attributes
			createArrayOfStringTypeDefinition(queryBaseName+"ReturnAttribute");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Error creating Attributes for Query {} - {}",queryBaseName,e.toString());
			return;
		}
		
		//Query Request Objekt
		String requestObjectName = queryBaseName+Constants.suffixRequest;
		ModelImpl definition = new ModelImpl();
		definition.setType("object");
		Map<String, Property> props = new HashMap<String, Property>();
		props.put("restrictions",new RefProperty("#/definitions/"+queryBaseName+"Restriction"));
		props.put("returnAttributes",new RefProperty("#/definitions/"+queryBaseName+"ReturnAttribute"));
		definition.setProperties(props);
		this.swaggerDef.addDefinition(requestObjectName, definition);
		

		//Response
		//Type-Definition fuer Relation-Response
		String responseObjectName = queryBaseName+Constants.suffixResponse;
		definition = new ModelImpl();
		definition.setType("object");
		props = new HashMap<String, Property>();
		//reference to ServiceStatusData
		props.put("status", new RefProperty("#/definitions/ServiceStatusData"));
		//referenz zu Relation-Data Objekt bestehend aus Relation + Entity
		props.put("returnData",new ArrayProperty(new RefProperty("#/definitions/"+queryBaseName +Constants.suffixResponseData)));
		definition.setProperties(props);
		this.swaggerDef.addDefinition(responseObjectName, definition);
		
		
		//Path-Objekt erstellen
		String pathToRemove = bgeBaseUrl + swaggerDef.getBasePath();
		String restPath = query.getRest().getService().getUrl().substring(pathToRemove.length());
		List<String> tags = new ArrayList<String>();
		tags.add(entity.getId());
		
		Operation op = getBaseOperation(queryBaseName,restPath,tags);
		
		BodyParameter body = new BodyParameter();
		body.setRequired(true);
		body.setName("body");
		RefModel schema = new RefModel();
		schema.setReference("#/definitions/" + requestObjectName);
		body.setSchema(schema);
		//op.getParameters().get(0).
		op.addParameter(body);
		
		Response response = new Response();
		response.setDescription("returns " + responseObjectName);
		response.setSchema(new RefProperty("#/definitions/" + responseObjectName));
		op.addResponse("200", response);
		
		Path path = new Path();
		path.setPost(op);
		this.swaggerDef.getPaths().put(restPath, path);		
		log.debug("REST path {}",restPath);
		
		
		
		
	}

	
	private void createRelation(EntityDto entity,RelationDto relation) throws Exception{
		
		String relationBaseName = StringUtils.capitalize(getEntityId(entity))+StringUtils.capitalize(relation.getId());
		log.debug("Lege Relation {} an",relationBaseName);
		
		//Typ-Definitionen
		
		//create referenced type definitions
		List<String> relatedEntities = new ArrayList<String>();
		if(relation.getTargetEntityIdentifiers().size()!=1){
			log.warn("Relation {} relates to more than one targetEntities - unable to create it",relation.getId());
			return;
		}
		for(String targetEntityName:relation.getTargetEntityIdentifiers()){
			
			try {
				//custom entities
				//aus custom.ctcdynLock wird custom/ctcdynLock, dann passt es von der URL
				targetEntityName=targetEntityName.replace(".", "/");
				
				JsonNode definition = restCall(getEntityDefUri(targetEntityName));
				EntityDto relEntityDef = mapper.readValue(definition.toString(),EntityDto.class);
				
				//TypeDefinition
				log.debug("Lege related Entity {} an",getEntityId(relEntityDef)+"Data");
				createTypeDefinition(getEntityId(relEntityDef)+"Data",relEntityDef.getAttributes());
				relatedEntities.add(getEntityId(relEntityDef)+"Data");
				
				//Type-Definition fuer Entity-Restriction
				createRestrictionTypeDefinition(relationBaseName+"EntityRestriction",relEntityDef.getAttributes());
				//Type-Definition fuer Return Entity-Attributes
				createArrayOfStringTypeDefinition(relationBaseName+"EntityReturnAttribute");
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("Error getting Entity-Definition for related entity {} - {}",targetEntityName,e.toString());
				return;
			}
		}
		
		
		
		//Type-Definition fuer Relations-Attribute
		//Nomenklatur EntityIdRelationIdRelation
		createTypeDefinition(relationBaseName+"Relation",relation.getAttributes());
		//Type-Definition fuer Relations-Attribute-Restriction
		createRestrictionTypeDefinition(relationBaseName+"RelationRestriction",relation.getAttributes());
		//Type-Definition fuer Return Relations-Attribute
		createArrayOfStringTypeDefinition(relationBaseName+"RelationReturnAttribute");
		
		//Type-Definition fuer Relation-Request
		//4 Objekte
		String requestObjectName = relationBaseName+Constants.suffixRequestData;
		ModelImpl definition = new ModelImpl();
		definition.setType("object");
		Map<String, Property> props = new HashMap<String, Property>();
		props.put("relationRestrictions",new RefProperty("#/definitions/"+relationBaseName+"RelationRestriction"));
		props.put("entityRestrictions",new RefProperty("#/definitions/"+relationBaseName+"EntityRestriction"));
		props.put("returnEntityAttributes",new RefProperty("#/definitions/"+relationBaseName+"EntityReturnAttribute"));
		props.put("returnRelationAttributes",new RefProperty("#/definitions/"+relationBaseName+"RelationReturnAttribute"));
		definition.setProperties(props);
		this.swaggerDef.addDefinition(requestObjectName, definition);
		
		//Relation-Data Objekt bestehend aus Relation + Entity
		definition = new ModelImpl();
		definition.setType("object");
		props = new HashMap<String, Property>();
		props.put("relation",new RefProperty("#/definitions/"+relationBaseName+"Relation"));
		props.put("entity",new RefProperty("#/definitions/"+relatedEntities.get(0)));
		definition.setProperties(props);
		this.swaggerDef.addDefinition(relationBaseName+"Data", definition);
		
		//Response
		//Type-Definition fuer Relation-Response
		String responseObjectName = relationBaseName+Constants.suffixResponseData;
		definition = new ModelImpl();
		definition.setType("object");
		props = new HashMap<String, Property>();
		//reference to ServiceStatusData
		props.put("status", new RefProperty("#/definitions/ServiceStatusData"));
		//referenz zu Relation-Data Objekt bestehend aus Relation + Entity
		props.put("returnData",new ArrayProperty(new RefProperty("#/definitions/"+relationBaseName+"Data")));
		definition.setProperties(props);
		this.swaggerDef.addDefinition(responseObjectName, definition);
		
		//Path-Objekt erstellen
		String pathToRemove = bgeBaseUrl + swaggerDef.getBasePath();
		String restPath = relation.getRest().getService().getUrl().substring(pathToRemove.length());
		List<String> tags = new ArrayList<String>();
		tags.add(entity.getId());
		
		Operation op = getBaseOperation(relationBaseName,restPath,tags);
		
		BodyParameter body = new BodyParameter();
		body.setRequired(true);
		body.setName("body");
		RefModel schema = new RefModel();
		schema.setReference("#/definitions/" + requestObjectName);
		body.setSchema(schema);
		//op.getParameters().get(0).
		op.addParameter(body);
		
		Response response = new Response();
		response.setDescription("returns " + responseObjectName);
		response.setSchema(new RefProperty("#/definitions/" + responseObjectName));
		op.addResponse("200", response);
		
		Path path = new Path();
		path.setPost(op);
		this.swaggerDef.getPaths().put(restPath, path);
		
	}


	private Operation createBGEPostOperation(String pathName,OperationDto bgeOperation,String bgeOperationBaseName,List<String> tags) throws Exception{

		//String operationBaseName = operation.getId() + StringUtils.capitalize(getEntityId(entity));

		Operation op = new Operation();
		op.setOperationId(bgeOperationBaseName);
		op.setTags(tags);
		List<String> contentTypes = new ArrayList<String>();
		contentTypes.add("application/json");
		op.setConsumes(contentTypes);
		op.setProduces(contentTypes);

		//Parameters
		List<Parameter> parameters = new ArrayList<Parameter>();
		//Session-ID
		QueryParameter session = new QueryParameter();
		session.setName("sessionId");
		session.setRequired(true);
		session.setType("string");
		session.setDescription("Session-ID");
		op.addParameter(session);

		//Path parameter
		Pattern pattern = Pattern.compile("\\{([^\\}]*)\\}");
		Matcher matcher = pattern.matcher(pathName);
		List<String> pathParams = new ArrayList<String>();
		while (matcher.find()) {
			String pathVar = matcher.group().replace("{", "").replace("}", "");
			PathParameter pParam = new PathParameter();
			pParam.setRequired(true);
			pParam.setName(pathVar);
			pParam.setType("string");
			op.addParameter(pParam);
			pathParams.add(pathVar);
		}

		//Body-Parameter
		//Path Variablen ausfiltern
		List<AttributeDto> requestParams = bgeOperation.getAttributes()
				.stream()
				.filter(attr -> !pathParams.contains(attr.getId()))
				.collect(Collectors.toList());

		String requestDefinition = null;
		requestDefinition = bgeOperationBaseName+Constants.suffixRequestData;
		if (!requestParams.isEmpty()){
			requestDefinition = bgeOperationBaseName+Constants.suffixRequestData;
		}
		
		
		String responseDefinition = null;
		if (!bgeOperation.getReturnAttributes().isEmpty()){
			responseDefinition = bgeOperationBaseName+Constants.suffixResponseData;
		}

		//Request
		boolean createBodyParam = false;
		//log.debug("Creating request definition {}",requestDefinition);
		if(!requestParams.isEmpty()){
			try {
				createTypeDefinition(requestDefinition,requestParams);
			} catch (Exception e) {
				log.error("Error creating request attributes for operation {} : {}",bgeOperation.getId(),e.getMessage());
				throw new Exception();
			}
			createBodyParam = true;
		}

		
		//SubOperations = weitere Properties mit Referenz auf Definitionen
		if (bgeOperation.getSubOperations() != null){
			//requestDefinition = bgeOperationBaseName+Constants.suffixRequestData;
			for (SubOperationDto subOp : bgeOperation.getSubOperations()){
				log.debug("Lege Sub-Operation {} / {} an",bgeOperationBaseName,subOp.getId());
				//Request Definitionen
				if(!subOp.getAttributes().isEmpty()){
					String subOpRequestDefinition = bgeOperationBaseName+ StringUtils.capitalize(subOp.getId()) + Constants.suffixRequestData;
					createTypeDefinition(subOpRequestDefinition,subOp.getAttributes());

					switch(subOp.getCardinality()){
					case "1":
						RefProperty refProp = new RefProperty("#/definitions/"+subOpRequestDefinition);
						refProp.setRequired(true);
						//addPropertyToDefinition(requestDefinition,subOpRequestDefinition,refProp);
						addPropertyToDefinition(requestDefinition,subOp.getId(),refProp);
						break;
					case "0..1":
						addPropertyToDefinition(requestDefinition,subOp.getId(),new RefProperty("#/definitions/"+subOpRequestDefinition));
						break;
					case "0..n":
						ArrayProperty arrProp = new ArrayProperty();
						arrProp.setRequired(false);
						arrProp.setItems(new RefProperty("#/definitions/"+subOpRequestDefinition));
						addPropertyToDefinition(requestDefinition,subOp.getId(),arrProp);
						
						break;
					case "1..n":
						ArrayProperty arrPropRequired = new ArrayProperty();
						arrPropRequired.setRequired(true);
						arrPropRequired.setItems(new RefProperty("#/definitions/"+subOpRequestDefinition));
						addPropertyToDefinition(requestDefinition,subOp.getId(),arrPropRequired);

						break;
					default:	
						log.error("Kardinalitaet {} unbekannt",subOp.getCardinality());
						throw new Exception();
					}
					createBodyParam = true;
				}
			}
		}

		if (createBodyParam){
			BodyParameter body = new BodyParameter();
			body.setRequired(true);
			body.setName("body");

			RefModel schema = new RefModel();
			//ModelImpl schema = new ModelImpl();

			schema.setReference("#/definitions/" + requestDefinition);
			body.setSchema(schema);

			//op.getParameters().get(0).
			op.addParameter(body);
		} else {
			//Empty body Object
			BodyParameter body = new BodyParameter();
			body.setRequired(false);
			body.setName("body");
			ModelImpl schema = new ModelImpl();
			schema.setType("object");
			body.setSchema(schema);
			op.addParameter(body);
		}

		//Response-Data
		if (!bgeOperation.getReturnAttributes().isEmpty()){
			log.debug("Creating response definition {}",responseDefinition);
			//log.debug("Creating response definition for operation {}, entity {}",operation.getId(),StringUtils.capitalize(getEntityId(entity)));
			try {
				createTypeDefinition(responseDefinition,bgeOperation.getReturnAttributes());
			} catch (Exception e) {
				log.error("Error creating response attributes for operation {} : {}",bgeOperation.getId(),e.getMessage());
				throw new Exception();
			}
		}
		createResponseDefinition(bgeOperationBaseName+Constants.suffixResponse,responseDefinition);
		Response response = new Response();
		response.setDescription("returns " + bgeOperationBaseName+Constants.suffixResponse);
		response.setSchema(new RefProperty("#/definitions/" + bgeOperationBaseName+Constants.suffixResponse));
		op.addResponse("200", response);


		return op;


	}

	/**
	 * @param name
	 * @param responseDefinition
	 */
	private void createResponseDefinition(String name,String responseDefinition){
		createResponseDefinition(name,responseDefinition,false);
		
	}

	/**
	 * @param name
	 * @param responseDefinition
	 * @param isArray
	 */
	private void createResponseDefinition(String name,String responseDefinition,boolean isArray){
		ModelImpl definition = new ModelImpl();
		definition.setType("object");
		Map<String, Property> props = new HashMap<String, Property>();
		//reference to ServiceStatusData
		props.put("status", new RefProperty("#/definitions/ServiceStatusData"));

		//return data
		if (responseDefinition!=null){
			//model.get
			if (isArray){
				props.put("returnData",new ArrayProperty(new RefProperty("#/definitions/" + responseDefinition)));
			} else {
				props.put("returnData", new RefProperty("#/definitions/" + responseDefinition));
			}
		}
		definition.setProperties(props);
		this.swaggerDef.addDefinition(name, definition);
	}
	
	/**
	 * @param opName
	 * @param restPath
	 * @param tags
	 * @return
	 */
	private Operation getBaseOperation(String opName,String restPath,List<String> tags){
		
		
		Operation op = new Operation();
		op.setOperationId(opName);
		op.setTags(tags);
		List<String> contentTypes = new ArrayList<String>();
		contentTypes.add("application/json");
		op.setConsumes(contentTypes);
		op.setProduces(contentTypes);

		//Parameters
		//Session-ID
		QueryParameter session = new QueryParameter();
		session.setName("sessionId");
		session.setRequired(true);
		session.setType("string");
		session.setDescription("Session-ID");
		op.addParameter(session);

		//Path parameter
		Pattern pattern = Pattern.compile("\\{([^\\}]*)\\}");
		Matcher matcher = pattern.matcher(restPath);
		List<String> pathParams = new ArrayList<String>();
		while (matcher.find()) {
			String pathVar = matcher.group().replace("{", "").replace("}", "");
			PathParameter pParam = new PathParameter();
			pParam.setRequired(true);
			pParam.setName(pathVar);
			pParam.setType("string");
			op.addParameter(pParam);
			pathParams.add(pathVar);
		}
		
		return op;
	}
	
	private void createArrayOfStringTypeDefinition(String name){
		ArrayModel definition = new ArrayModel();
		definition.setItems(new StringProperty());
		this.swaggerDef.addDefinition(name, definition);
	}
	
	private void createRestrictionTypeDefinition(String name,List<AttributeDto> attributes) throws Exception{
		ModelImpl definition = new ModelImpl();
		definition.setType("object");
		Map<String, Property> props = new HashMap<String, Property>();
		for (AttributeDto attr : attributes){
			//prop = new StringProperty();
			if (attr.getDataType()!=null){
				//if (attr.getRestrictable())
				props.put(attr.getId(), mapAttributeToRestrictionProperty(attr));

			}
			if (attr.getType()!=null){
				log.debug("Spezialfall {}",attr.getType().getClass().getName());
			}
		}
		definition.setProperties(props);
		definition.setTitle(name);
		this.swaggerDef.getDefinitions().put(name, definition);
	}


	private void createTypeDefinition(String name,List<AttributeDto> attributes) throws Exception{
		ModelImpl definition = new ModelImpl();
		definition.setType("object");
		Map<String, Property> props = new HashMap<String, Property>();
		Property prop;
		//props.put("", value)
		for (AttributeDto attr : attributes){
			//prop = new StringProperty();
			if (attr.getDataType()!=null){
				props.put(attr.getId(), mapAttributeToProperty(attr));

			}
			if (attr.getType()!=null){
				log.debug("Spezialfall {} {}",attr.getType().getClass().getSimpleName(),attr.getType().getClass().getName());
				switch (attr.getType().getClass().getSimpleName()){
				case "ListTypeDto":
					ListTypeDto dto = (ListTypeDto) attr.getType();
					props.put(attr.getId(),new ArrayProperty(new StringProperty()));
				break;	
				default:
					log.warn("Unknown type {}",attr.getType().getClass().getSimpleName());
				}
			}
		}
		definition.setProperties(props);
		definition.setTitle(name);
		this.swaggerDef.getDefinitions().put(name, definition);
	}

	private Property mapAttributeToProperty(AttributeDto attr) throws Exception{
		Property prop;

		switch (attr.getDataType()){
		case "STRING":
			prop = new StringProperty();
			StringProperty strProp = new StringProperty();
			prop = strProp;
			//prop.
			break;
		case "NUMERIC":
			prop = new DecimalProperty("BigDecimal"); 
			break;
		case "DATE":
			prop = new DateTimeProperty(); 
			break;
		case "BOOLEAN":
			prop = new io.swagger.models.properties.BooleanProperty();
			break;
		default:
			throw new Exception("Unknown Attribute-Type: " + attr.getDataType());
		}

		return prop;
	}
	
	private Property mapAttributeToRestrictionProperty(AttributeDto attr) throws Exception{
		switch (attr.getDataType()){
		case "STRING":
			return new RefProperty("#/definitions/RestrictionsString");
		case "NUMERIC":
			return new RefProperty("#/definitions/RestrictionsNumeric");
		case "BOOLEAN":
			return new RefProperty("#/definitions/RestrictionsBoolean");
		case "DATE":
			return new RefProperty("#/definitions/RestrictionsDate");			
		default:
			throw new Exception("Cannot map attribute "+attr.getId()+" Attribute-Type: " + attr.getDataType() + " to restriction");
		}

	}	
	
	private void addPropertyToDefinition(String definitionName, String propertyName,Property prop){
		if(!this.swaggerDef.getDefinitions().containsKey(definitionName)){

			log.debug("Keine Request-Definition {} vorhanden - lege eine an",definitionName);
			ModelImpl definition = new ModelImpl();
			definition.setType("object");
			Map<String, Property> props = new HashMap<String, Property>();
			props.put(propertyName, prop);
			definition.setTitle(definitionName);
			definition.setProperties(props);

			log.debug("{}",props);
			this.swaggerDef.getDefinitions().put(definitionName, definition);
		} else {
			this.swaggerDef.getDefinitions().get(definitionName).getProperties().put(propertyName, prop);
		}

	}	


	private JsonNode restCall(UriComponents uriComponents) throws Exception{

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> entity = new HttpEntity<>(null, requestHeaders);

		//alte MessageConverter speichern
		List<HttpMessageConverter<?>> oldConverters = new ArrayList<HttpMessageConverter<?>>();
		oldConverters.addAll(restTemplate.getMessageConverters());

		//String Message-Konverter  anlegen
		List<HttpMessageConverter<?>> stringConverter = new ArrayList<HttpMessageConverter<?>>();
		stringConverter.add(new StringHttpMessageConverter());
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		ResponseEntity<String> queryResponse;
		log.debug("GET {}",uriComponents.toUriString());
		try {
			queryResponse = restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, entity, String.class);
		} catch (HttpClientErrorException e) {
			log.error("Error performing REST-Call: {} - {}",e.getStatusCode().name(),e.getResponseBodyAsString());
			restTemplate.setMessageConverters(oldConverters);
			throw new Exception("Entity query");
		}
		restTemplate.setMessageConverters(oldConverters);

		String body = null;
		JsonNode definition = null;
		ObjectMapper mapper = new ObjectMapper();

		if (queryResponse.hasBody()) {
			body = queryResponse.getBody();
			try {
				JsonNode response = mapper.readTree(body);

				if (response.path("status").isMissingNode()){
					throw new Exception("REST - Query Exception: No Response-Status");
				}
				//log.debug(response.path("status").toString());
				ServiceStatusData status = mapper.readValue(response.path("status").toString(),ServiceStatusData.class);
				if (!status.getSuccess()){
					throw new Exception("REST - Query Exception: " + status.getMessage());
				}

				if (response.path("definition").isMissingNode()){
					throw new Exception("REST - Query Exception: No definition");
				}
				definition = response.path("definition");
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Mapping exception");
			}
		} else {
			throw new Exception("REST - Query Exception: No Response-Body");
		}

		//log.debug(definition.toString());

		return definition;
	}

	private UriComponents getBGEDefUri(){

		return UriComponentsBuilder
				.fromUriString(bgeBaseUrl +Constants.bgeDefPath)
				.build()
				.encode();
	}

	private UriComponents getEntityDefUri(String entityName){

		return UriComponentsBuilder
				.fromUriString(bgeBaseUrl +Constants.entityDefPath)
				.queryParam("detailed", "true")
				.build()
				.expand(entityName)
				.encode();
	}

	private UriComponents getEntityDefUri(EntityInfoDto entity){
		String entityId = getEntityId(entity);
		String uriString;
		if(entity.getCustom()!=null){
			uriString = bgeBaseUrl +Constants.customEntityDefPath;
		} else {
			uriString = bgeBaseUrl +Constants.entityDefPath;
		}

		return UriComponentsBuilder
				.fromUriString(uriString)
				.queryParam("detailed", "true")
				.build()
				.expand(entityId)
				.encode();
	}

	@PostConstruct
	public void initSwaggerGenerator() throws Exception{
		log.debug("Init generator");
		this.swaggerDef = new Swagger();
		swaggerDef.setDefinitions(new HashMap<String,Model>());
		swaggerDef.setPaths(new HashMap<String,Path>());
		
		//ObjectMapper
		mapper = new ObjectMapper();
		mapper.addMixIn(TypeDto.class, TypeDtoMixIn.class);
		
		JsonNode bgedefinition = restCall(getBGEDefUri());
		//ObjectMapper mapper = new ObjectMapper();
		//mapper.addMixIn(TypeDto.class, TypeDtoMixIn.class);
		SystemInfoDto dto = mapper.readValue(bgedefinition.toString(), SystemInfoDto.class);
		log.debug(dto.getVersion());

		Info info = new Info();
		info.setTitle("Swagger for Command BGE");
		info.setDescription("Swagger API for FNT Command Business Gateway");
		info.setVersion(dto.getVersion());

		swaggerDef.setInfo(info );
		swaggerDef.setHost(swaggerHost);
		//swaggerDef.setBasePath("/axis/api/rest");
		swaggerDef.setBasePath("/axis");
		
		URL url = new URL(this.bgeBaseUrl);
		this.swaggerHost = url.getAuthority();
		
		log.debug(this.swaggerHost);

		List<Scheme> schemes = new ArrayList<Scheme>();
		schemes.add(Scheme.HTTPS);

		if (url.getProtocol().toLowerCase().equals("https")) {
			schemes.add(Scheme.HTTPS);
		} else if (url.getProtocol().toLowerCase().equals("http")) {
			schemes.add(Scheme.HTTP);
		} else {
			log.warn("Unknown protocol {}",url.getProtocol());
		}
		
		swaggerDef.setSchemes(schemes);

		//generische erzeugen
		//ServiceStatusData
		ModelImpl definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("ServiceStatusData");
		Map<String, Property> props = new HashMap<String, Property>();
		props.put("errorCode", new IntegerProperty());
		props.put("message", new StringProperty());
		props.put("subErrorCode", new StringProperty());
		props.put("success", new BooleanProperty());
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("ServiceStatusData", definition);
		
		//Restrictions
		ArrayList<String> reqParams = new ArrayList<String>();
		reqParams.add("operator");
		reqParams.add("value");
		//RestrictionsString
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("RestrictionsString");
		props = new HashMap<String, Property>();
		props.put("operator", new StringProperty());
		props.put("value", new StringProperty());
		definition.setRequired(reqParams);
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("RestrictionsString", definition);
		//RestrictionsNumeric
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("RestrictionsNumeric");
		props = new HashMap<String, Property>();
		props.put("operator", new StringProperty());
		props.put("value", new DecimalProperty());
		definition.setRequired(reqParams);
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("RestrictionsNumeric", definition);
		//RestrictionsNumeric
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("RestrictionsBoolean");
		props = new HashMap<String, Property>();
		props.put("operator", new StringProperty());
		props.put("value", new BooleanProperty());
		definition.setRequired(reqParams);
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("RestrictionsBoolean", definition);		
		//RestrictionsNumeric
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("RestrictionsDate");
		props = new HashMap<String, Property>();
		props.put("operator", new StringProperty());
		props.put("value", new DateTimeProperty());
		definition.setRequired(reqParams);
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("RestrictionsDate", definition);		
		
		//Login
		
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("LoginRequest");
		props = new HashMap<String, Property>();
		props.put("manId", new StringProperty());
		props.put("password", new StringProperty());
		props.put("user", new StringProperty());
		props.put("userGroupName", new StringProperty());
		reqParams = new ArrayList<String>();
		reqParams.add("manId");
		reqParams.add("password");
		reqParams.add("user");
		reqParams.add("userGroupName");		
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("LoginRequest", definition);		
		
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("LoginResponse");
		props = new HashMap<String, Property>();
		props.put("status", new RefProperty("#/definitions/ServiceStatusData"));
		props.put("sessionId", new StringProperty());
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("LoginResponse", definition);
		
		//Path-Objekt erstellen
		String restPath = "/api/rest/login";
		List<String> tags = new ArrayList<String>();
		tags.add("REST");
		Operation op = new Operation();
		op.setOperationId("login");
		op.setTags(tags);
		List<String> contentTypes = new ArrayList<String>();
		contentTypes.add("application/json");
		op.setConsumes(contentTypes);
		op.setProduces(contentTypes);
	
		BodyParameter body = new BodyParameter();
		body.setRequired(true);
		body.setName("body");
		RefModel schema = new RefModel();
		schema.setReference("#/definitions/LoginRequest");
		body.setSchema(schema);
		//op.getParameters().get(0).
		op.addParameter(body);
		
		Response response = new Response();
		response.setDescription("returns LoginResponse");
		response.setSchema(new RefProperty("#/definitions/LoginResponse"));
		op.addResponse("200", response);
		
		Path path = new Path();
		path.setPost(op);
		this.swaggerDef.getPaths().put(restPath, path);
		
		
		//Logout
		/*
		definition = new ModelImpl();
		definition.setType("object");
		definition.setTitle("BaseResponse");
		props = new HashMap<String, Property>();
		props.put("status", new RefProperty("#/definitions/ServiceStatusData"));		
		definition.setProperties(props);
		this.swaggerDef.getDefinitions().put("BaseResponse", definition);
		*/
		//Path-Objekt erstellen
		restPath = "/api/rest/logout";
		tags = new ArrayList<String>();
		tags.add("REST");
		op = getBaseOperation("logout",restPath,tags);
		response = new Response();
		response.setDescription("returns LogoutResponse");
		response.setSchema(new RefProperty("#/definitions/ServiceStatusData"));
		op.addResponse("200", response);
		path = new Path();
		path.setPost(op);
		this.swaggerDef.getPaths().put(restPath, path);		
		
		//swaggerDef.addDefinition("ServiceStatusData", definition);


	}


	public Swagger getSwaggerDef() {
		return swaggerDef;
	}


	public void setSwaggerDef(Swagger swaggerDef) {
		this.swaggerDef = swaggerDef;
	}


}

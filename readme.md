# OpenAPI Specificatio 2.0 Generator for FNT Command

Creates OAS 2.0 (fka Swagger-Specs) for FNT-Command Business-Gateway Entities.

Specs are created for all (or selected) entities with operations, relations, queries.

## Run

Example 

> CMD_BASE_URL=https://rmdb-dev.workspace.witcom.de GENERATOR_OUTPUT=./myspec.json ENTITY_LIST=easysearch,bearer,campus,person java -jar bge2swagger-xxx.jar

Creates a file ./myspec.json for the entities easysearch,bearer,campus,person

Configuration is done with ENV-Variables

* CMD_BASE_URL - required: Base-Url of Command-Instance 
* GENERATOR_OUTPUT - optional. Defines the file the specs are written to. defaults to ./bge-swagger.json
* ENTITY_LIST - optional comma-separated list of entities to generate, defaults to all entities


## ToDos

* Support 

Run like this


and it creates a file ./myspec.json for the entities easysearch,bearer,campus,person.

Parameters

CMD_BASE_URL - required: Base-Url of Command-Instance 
GENERATOR_OUTPUT - optional. Defines the file the specs are written to. defaults to ./bge-swagger.json
ENTITY_LIST - optional comma-separated list of entities to generate, defaults to all entities

WiTCOM best practice

campus,building,room,floor,zone,custom.ctcdynPostalAddress,chassis,deviceall,masterdevice,module,passivemodule,switchcabinet,warehouse,organisation,person,bearer,logicalport,serviceTelcoPath,serviceTelcoUnroutedPath,serviceTelcoMultipoint,serviceTelcoPointToPoint,serviceTelcoUnroutedMultipoint,serviceTypeDefinition
		

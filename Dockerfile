FROM eclipse-temurin:17-jdk-alpine
ENV OPENAPI_GENERATOR_RELEASE=7.2.0

RUN adduser --system --uid 1001 app

# Create application directory
RUN mkdir -p /app

WORKDIR /app

RUN cp -r /app-build/bge2swagger.jar /app/
RUN wget --no-check-certificate https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/${OPENAPI_GENERATOR_RELEASE}/openapi-generator-cli-${OPENAPI_GENERATOR_RELEASE}.jar -O /app/openapi-generator-cli.jar

CMD ["sh"]
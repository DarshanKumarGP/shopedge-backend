# Use latest OpenJDK 17 as base image
FROM eclipse-temurin:17-jre-alpine

# Set working directory inside the container
WORKDIR /ShopEdge

# Copy the built JAR into the image
COPY target/shopedge-backend-0.0.1-SNAPSHOT.jar ShopEdgeBackend.jar

# Create a non-root user for security
RUN addgroup -g 1001 -S shopedge \
 && adduser -S shopedge -u 1001 -G shopedge \
 && chown -R shopedge:shopedge /ShopEdge

USER shopedge

# Expose the application port
EXPOSE 9090

# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

# Run the Spring Boot application without overriding config
ENTRYPOINT ["java", "-jar", "ShopEdgeBackend.jar"]

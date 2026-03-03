# imaginea de baza - foloseste openjdk 21
# versiunea de Java folosita de container
FROM amazoncorretto:21-alpine

# irectorul de lucru din container la /app
WORKDIR /app

# copiaza fisierul JAR din directorul target/ in container la calea /app/app.jar
COPY target/*.jar app.jar

# documenteaza ca aplicatia asculta pe port 8082
EXPOSE 8082

# se executa cand container-ul porneste
# porneste aplicatia Spring Boot din fisierul app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
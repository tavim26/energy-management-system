#imaginea de baza pt container (slim pt dimensiune redusa)
FROM amazoncorretto:21-alpine


#setare director lucru in container (toate comenzile ulterioare vor fi executate in director)
WORKDIR /app

#copiaza fisier JAR din host ( target/) in container
COPY target/*.jar app.jar

#documenteaza portul pe care asculta aplicatia
EXPOSE 8081

#comanda care se executa la pornirea container-ului
ENTRYPOINT ["java", "-jar", "app.jar"]
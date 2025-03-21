# Prerequisites

In order to be able to submit your assignement, you should have the following installed:
* JDK >= 17
* Maven
* Postman / any other tool that allows you to hit the application's endpoints
* Any versioning tool
* Any IDE that allows you to run the application


# How to

#### Run the application
The application should be run as a SpringBootApplication. Below is a quick guide on how to do that via IntelliJ:
* Edit Configuration 
   * Add New Configuration (Spring Boot)
     * Change the **Main class** to **ing.assessment.INGAssessment**
       * Run the app.

#### Connect to the H2 database
Access the following url: **http://localhost:8080/h2-console/**
 * **Driver Class**: _**org.h2.Driver**_
 * **JDBC URL**: _**jdbc:h2:mem:testdb**_
 * **User Name**: _**sa**_
 * **Password**: **_leave empty_**



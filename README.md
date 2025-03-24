# Prerequisites

In order to be able to run the app, you will need:
* JDK >= 17
* Maven
* Postman / any other tool that allows you to hit the application's endpoints
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

# Endpoints

You should call the endpoints using your localhost URL, most likely it will be: http://localhost:8080

## Orders

You can find information about the orders by hitting the following endpoints:


* #### Get all orders

        /orders/all
  
  * #### Get information about a specific order

          /orders/{id}

      Replace {id} in your path with the ID of the order you want to query. Passing a non-existent ID will result in a OrderNotFoundException.

    * #### Place a new order

            /orders/place

        This alone will not work, you will receive an InvalidOrderException. You need to add a request body in the following format:

      ```
          {
              productId: quantity
          }
      ```
      So, for example:

      ```
          {
              "1": 2,
              "2": 2,
              "3": 1
          }
      ```
    
      If everything is set up correctly, the response should look something like this:

      ```
      {
          "id": 1,
          "timestamp": "2025-03-24T13:37:58.670+00:00",
          "orderProducts": [
              {
                  "productId": 1,
                  "quantity": 2,
                  "name": "Shoes",
                  "totalCost": 800.0
              },
              {
                  "productId": 2,
                  "quantity": 2,
                  "name": "Shirt",
                  "totalCost": 200.0
              },
              {
                  "productId": 3,
                  "quantity": 1,
                  "name": "Jeans",
                  "totalCost": 200.0
              }
          ],
          "orderCost": 1080.0,
          "discount": 120.0,
          "deliveryCost": 0,
          "deliveryTime": 2
      }
      ```

    You will get back a response with all the products that were ordered, including how much was spent on each product category.
    Also, you will get information about delivery cost, delivery time, total cost of the order and discounts that were applied, if any.


## Products

You can find information about the products by hitting the following endpoints:


* #### Get all products

        /products

* #### Get information about a specific product

        /products/{id}

Replace {id} in your path with the ID of the product you want to query. Passing a non-existent ID will result in a ProductNotFoundException.
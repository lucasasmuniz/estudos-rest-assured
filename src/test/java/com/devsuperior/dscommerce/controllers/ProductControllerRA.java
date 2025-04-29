package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ProductControllerRA {

	private Long existingId, nonExistingId;
	private String productName;
	private Map<String, Object> postProductInstance;
	private String adminToken, clientToken, invalidToken;
	
	@BeforeEach
	public void setUp() {
		baseURI = "http://localhost:8080";
		existingId = 2L;
		nonExistingId = 300L;
		productName = "pc";
		adminToken = TokenUtil.obtainAccessToken("alex@gmail.com", "123456");
		clientToken = TokenUtil.obtainAccessToken("maria@gmail.com", "123456");
		invalidToken = adminToken + "bad";
		
		
		postProductInstance = new HashMap<>();
		postProductInstance.put("name", "Meu produto");
		postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
		postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
		postProductInstance.put("price", 50.0);
		
		List<Map<String, Object>> categories = new ArrayList<>();
		
		Map<String, Object> category1 = new HashMap<>();
		category1.put("id", 2);
		
		Map<String, Object> category2 = new HashMap<>();
		category2.put("id", 3);
		
		categories.add(category1);
		categories.add(category2);
		
		postProductInstance.put("categories", categories);
	}
	
	@Test
	public void findByIdShouldReturnProductDTOAndOkWhenExistingId() {	
		given()
			.get("/products/{id}", existingId)
		.then()
			.statusCode(200)
			.body("id", is(2))
			.body("name", equalTo("Smart TV"))
			.body("description", equalTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."))
			.body("price", is(2190.0F))
			.body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
			.body("categories.id", hasItems(2,3))
			.body("categories.name", hasItems("Eletrônicos","Computadores"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenNonExistingId() {
		given()
			.get("/products/{id}", nonExistingId)
		.then()
			.statusCode(404);
	}
	
	@Test
	public void findAllShouldReturnProductPageWhenProductNameIsEmpty() {
		given()
			.get("/products")
		.then()
			.statusCode(200)
			.body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));
	}
	
	
	@Test
	public void findAllShouldReturnProductsPageWhenProductNameIsNotEmpty() {
		given()
			.get("/products?name={productName}", productName)
		.then()
			.statusCode(200)
			.body("content.id[1]", is(6))
			.body("content.name[1]", equalTo("PC Gamer Ex"))
			.body("content.price[1]", is(1350.0F))
			.body("content.imgUrl[1]", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/6-big.jpg"))
			.body("totalElements", is(21));
	}
	
	@Test
	public void findAllShouldReturnProductsPageWithPriceGreaterThan2000() {
		given()
			.get("/products?size=25")
		.then()
			.statusCode(200)
			.body("content.findAll { it.price>2000 }.name", hasItems("PC Gamer Boo", "PC Gamer Foo"));
	}
	
	@Test
	public void insertShouldReturnProductsDTOAndCreatedWhenAdminUserAndValidData() {
		JSONObject jsonObject = new JSONObject(postProductInstance);
		
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "bearer " + adminToken)
			.body(jsonObject)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(201)
			.body("name", equalTo("Meu produto"))
			.body("price", is(50.0F))
			.body("categories.id", hasItems(2,3));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminUserAndInvalidName() {
		postProductInstance.put("name", "a");
		JSONObject jsonObject = new JSONObject(postProductInstance);
		
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "bearer " + adminToken)
			.body(jsonObject)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.fieldName", hasItem("name"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminUserAndInvalidDescription() {
		postProductInstance.put("description", null);
		JSONObject jsonObject = new JSONObject(postProductInstance);
		
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "bearer " + adminToken)
			.body(jsonObject)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.fieldName", hasItem("description"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminUserAndPriceIsNegative() {
		postProductInstance.put("price", -3);
		JSONObject jsonObject = new JSONObject(postProductInstance);
		
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "bearer " + adminToken)
			.body(jsonObject)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.fieldName", hasItem("price"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminUserAndPriceIsZero() {
		postProductInstance.put("price", 0);
		JSONObject jsonObject = new JSONObject(postProductInstance);
		
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "bearer " + adminToken)
			.body(jsonObject)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.fieldName", hasItem("price"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminUserAndCategoriesIsEmpty() {
		postProductInstance.put("categories", new ArrayList<>());
		JSONObject jsonObject = new JSONObject(postProductInstance);
		
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "bearer " + adminToken)
			.body(jsonObject)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(422)
			.body("errors.fieldName", hasItem("categories"));
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientUserAndValidData() {
		JSONObject jsonObject = new JSONObject(postProductInstance);
		
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "bearer " + clientToken)
			.body(jsonObject)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenClientUserAndValidData() {
		JSONObject jsonObject = new JSONObject(postProductInstance);
		
		given()
			.header("Content-Type", "application/json")
			.header("Authorization", "bearer " + invalidToken)
			.body(jsonObject)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post("/products")
		.then()
			.statusCode(401);
	}
}

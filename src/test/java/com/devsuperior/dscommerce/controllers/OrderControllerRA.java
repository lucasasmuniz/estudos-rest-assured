package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;

public class OrderControllerRA {
	
	private Long existingId, nonExistingId;
	private String adminToken, clientToken, invalidToken;
	
	@BeforeEach
	public void setUp(){
		baseURI = "http://localhost:8080";
		existingId = 1L;
		nonExistingId = 300L;
		adminToken = TokenUtil.obtainAccessToken("alex@gmail.com", "123456");
		clientToken = TokenUtil.obtainAccessToken("maria@gmail.com", "123456");
		invalidToken = adminToken + "123444";
	}
	
	@Test
	public void findByIdShouldReturnOrderDTOAndOkWhenAdminLoggedAndOrderIdExists() {
		given()
			.header("Authorization", "bearer " + adminToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", existingId)
		.then()
			.statusCode(200)
			.body("id", is(1))
			.body("client.name", equalTo("Maria Brown"))
			.body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"));
	}
	
	@Test
	public void findByIdShouldReturnOrderDTOAndOkWhenClientLoggedAndOrderIdExistsAndOrderBelongsUser() {
		given()
			.header("Authorization", "bearer " + clientToken)
			.accept(ContentType.JSON)
		.when()
			.get("/orders/{id}", existingId)
		.then()
			.statusCode(200)
			.body("id", is(1))
			.body("client.name", equalTo("Maria Brown"))
			.body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"));
	}
	
	@Test
	public void findByIdShouldReturnForbbidenWhenClientLoggedAndOrderIdExistsAndOrderDoesNotBelongsUser() {
		given()
			.header("Authorization", "bearer " + clientToken)
		.when()
			.get("/orders/{id}", 2)
		.then()
			.statusCode(403);
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenAdminLoggedAndOrderIdDoesNotExists() {
		given()
			.header("Authorization", "bearer " + adminToken)
		.when()
			.get("/orders/{id}", nonExistingId)
		.then()
			.statusCode(404);
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenClientLoggedAndOrderIdDoesNotExists() {
		given()
			.header("Authorization", "bearer " + clientToken)
		.when()
			.get("/orders/{id}", nonExistingId)
		.then()
			.statusCode(404);
	}
	
	
	@Test
	public void findByIdShouldReturnUnauthorizedWhenInvalidUserLoggedAndOrderIdExists() {
		given()
			.header("Authorization", "bearer " + invalidToken)
		.when()
			.get("/orders/{id}", existingId)
		.then()
			.statusCode(401);
	}
}

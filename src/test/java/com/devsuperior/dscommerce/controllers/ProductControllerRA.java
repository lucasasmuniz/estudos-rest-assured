package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProductControllerRA {

	private Long existingId, nonExistingId;
	private String productName;
	
	@BeforeEach
	public void setUp() {
		baseURI = "http://localhost:8080";
		existingId = 2L;
		nonExistingId = 300L;
		productName = "pc";
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
	public void findAllShouldReturnProductsPageWhenProductNameIsEmpty() {
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
	public void findAllShouldReturnProductPageWithPriceGreaterThan2000() {
		given()
			.get("/products?size=25")
		.then()
			.statusCode(200)
			.body("content.findAll { it.price>2000 }.name", hasItems("PC Gamer Boo", "PC Gamer Foo"));
	}
}

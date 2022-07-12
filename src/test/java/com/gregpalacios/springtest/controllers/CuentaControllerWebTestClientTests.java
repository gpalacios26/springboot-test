package com.gregpalacios.springtest.controllers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gregpalacios.springtest.models.Cuenta;
import com.gregpalacios.springtest.models.TransaccionDto;

@Tag("integracion_wc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CuentaControllerWebTestClientTests {

	@Autowired
	private WebTestClient client;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
	}

	@Test
	@Order(1)
	void testTransferir() throws JsonProcessingException {
		// Given
		TransaccionDto dto = new TransaccionDto();
		dto.setCuentaOrigenId(1L);
		dto.setCuentaDestinoId(2L);
		dto.setBancoId(1L);
		dto.setMonto(new BigDecimal("100"));

		Map<String, Object> response = new HashMap<>();
		response.put("date", LocalDate.now().toString());
		response.put("status", "OK");
		response.put("mensaje", "Transferencia realizada con éxito");
		response.put("transaccion", dto);

		// When
		client.post().uri("/api/cuentas/transferir").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange()
				// Then
				.expectStatus().isOk().expectBody().jsonPath("$.mensaje").isNotEmpty().jsonPath("$.status")
				.value(is("OK")).jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
				.json(objectMapper.writeValueAsString(response));
	}

	@Test
	@Order(2)
	void testDetalle() {
		client.get().uri("/api/cuentas/1").exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.persona").isEqualTo("Andrés")
				.jsonPath("$.saldo").isEqualTo(900);
	}

	@Test
	@Order(3)
	void testDetalle2() {
		client.get().uri("/api/cuentas/2").exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody(Cuenta.class).consumeWith(response -> {
					Cuenta cuenta = response.getResponseBody();
					assertEquals("John", cuenta.getPersona());
					assertEquals("2100.00", cuenta.getSaldo().toPlainString());
				});
	}

	@Test
	@Order(4)
	void testListar() {
		client.get().uri("/api/cuentas").exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$[0].persona").isEqualTo("Andrés")
				.jsonPath("$[0].id").isEqualTo(1).jsonPath("$[0].saldo").isEqualTo(900).jsonPath("$[1].persona")
				.isEqualTo("John").jsonPath("$[1].id").isEqualTo(2).jsonPath("$[1].saldo").isEqualTo(2100).jsonPath("$")
				.isArray().jsonPath("$").value(hasSize(2));
	}

	@Test
	@Order(5)
	void testListar2() {
		client.get().uri("/api/cuentas").exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBodyList(Cuenta.class).consumeWith(response -> {
					List<Cuenta> cuentas = response.getResponseBody();
					assertNotNull(cuentas);
					assertEquals(2, cuentas.size());
					assertEquals(1L, cuentas.get(0).getId());
					assertEquals("Andrés", cuentas.get(0).getPersona());
					assertEquals(900, cuentas.get(0).getSaldo().intValue());
					assertEquals(2L, cuentas.get(1).getId());
					assertEquals("John", cuentas.get(1).getPersona());
					assertEquals("2100.0", cuentas.get(1).getSaldo().toPlainString());
				}).hasSize(2).value(hasSize(2));
	}

	@Test
	@Order(6)
	void testGuardar() {
		// Given
		Cuenta cuenta = new Cuenta(null, "Pepe", new BigDecimal("3000"));

		// When
		client.post().uri("/api/cuentas").contentType(MediaType.APPLICATION_JSON).bodyValue(cuenta).exchange()
				// Then
				.expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$.id").isEqualTo(3).jsonPath("$.persona").isEqualTo("Pepe").jsonPath("$.persona")
				.value(is("Pepe")).jsonPath("$.saldo").isEqualTo(3000);
	}

	@Test
	@Order(7)
	void testGuardar2() {
		// Given
		Cuenta cuenta = new Cuenta(null, "Pepa", new BigDecimal("3500"));

		// When
		client.post().uri("/api/cuentas").contentType(MediaType.APPLICATION_JSON).bodyValue(cuenta).exchange()
				// Then
				.expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Cuenta.class).consumeWith(response -> {
					Cuenta c = response.getResponseBody();
					assertNotNull(c);
					assertEquals(4L, c.getId());
					assertEquals("Pepa", c.getPersona());
					assertEquals("3500", c.getSaldo().toPlainString());
				});
	}

	@Test
	@Order(8)
	void testEliminar() {
		client.get().uri("/api/cuentas").exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBodyList(Cuenta.class).hasSize(4);

		client.delete().uri("/api/cuentas/3").exchange().expectStatus().isNoContent().expectBody().isEmpty();

		client.get().uri("/api/cuentas").exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBodyList(Cuenta.class).hasSize(3);

		client.get().uri("/api/cuentas/3").exchange().expectStatus().isNotFound().expectBody().isEmpty();
	}

}

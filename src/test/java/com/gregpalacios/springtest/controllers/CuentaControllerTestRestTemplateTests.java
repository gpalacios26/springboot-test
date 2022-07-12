package com.gregpalacios.springtest.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gregpalacios.springtest.models.Cuenta;
import com.gregpalacios.springtest.models.TransaccionDto;

@Tag("integracion_rt")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CuentaControllerTestRestTemplateTests {

	@Autowired
	private TestRestTemplate client;

	private ObjectMapper objectMapper;

	@LocalServerPort
	private int puerto;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
	}

	@Test
	@Order(1)
	void testTransferir() throws JsonMappingException, JsonProcessingException {
		TransaccionDto dto = new TransaccionDto();
		dto.setCuentaOrigenId(1L);
		dto.setCuentaDestinoId(2L);
		dto.setBancoId(1L);
		dto.setMonto(new BigDecimal("100"));

		ResponseEntity<String> response = client.postForEntity(crearUri("/api/cuentas/transferir"), dto, String.class);
		String json = response.getBody();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
		assertNotNull(json);
		assertTrue(json.contains("OK"));

		JsonNode jsonNode = objectMapper.readTree(json);
		assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
		assertEquals("100", jsonNode.path("transaccion").path("monto").asText());
		assertEquals(1L, jsonNode.path("transaccion").path("cuentaOrigenId").asLong());

		Map<String, Object> response2 = new HashMap<>();
		response2.put("date", LocalDate.now().toString());
		response2.put("status", "OK");
		response2.put("mensaje", "Transferencia realizada con éxito");
		response2.put("transaccion", dto);

		assertEquals(objectMapper.writeValueAsString(response2), json);
	}

	@Test
	@Order(2)
	void testDetalle() {
		ResponseEntity<Cuenta> respuesta = client.getForEntity(crearUri("/api/cuentas/1"), Cuenta.class);
		Cuenta cuenta = respuesta.getBody();

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());
		assertNotNull(cuenta);

		assertEquals(1L, cuenta.getId());
		assertEquals("Andrés", cuenta.getPersona());
		assertEquals("900.00", cuenta.getSaldo().toPlainString());
		assertEquals(new Cuenta(1L, "Andrés", new BigDecimal("900.00")), cuenta);
	}

	@Test
	@Order(3)
	void testListar() throws JsonProcessingException {
		ResponseEntity<Cuenta[]> respuesta = client.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
		List<Cuenta> cuentas = Arrays.asList(respuesta.getBody());

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());
		assertNotNull(cuentas);

		assertEquals(2, cuentas.size());
		assertEquals(1L, cuentas.get(0).getId());
		assertEquals("Andrés", cuentas.get(0).getPersona());
		assertEquals("900.00", cuentas.get(0).getSaldo().toPlainString());
		assertEquals(2L, cuentas.get(1).getId());
		assertEquals("John", cuentas.get(1).getPersona());
		assertEquals("2100.00", cuentas.get(1).getSaldo().toPlainString());

		JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(cuentas));
		assertEquals(1L, json.get(0).path("id").asLong());
		assertEquals("Andrés", json.get(0).path("persona").asText());
		assertEquals("900.0", json.get(0).path("saldo").asText());
		assertEquals(2L, json.get(1).path("id").asLong());
		assertEquals("John", json.get(1).path("persona").asText());
		assertEquals("2100.0", json.get(1).path("saldo").asText());
	}

	@Test
	@Order(4)
	void testGuardar() {
		Cuenta cuenta = new Cuenta(null, "Pepa", new BigDecimal("3800"));

		ResponseEntity<Cuenta> respuesta = client.postForEntity(crearUri("/api/cuentas"), cuenta, Cuenta.class);
		Cuenta cuentaCreada = respuesta.getBody();

		assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
		assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());
		assertNotNull(cuentaCreada);

		assertEquals(3L, cuentaCreada.getId());
		assertEquals("Pepa", cuentaCreada.getPersona());
		assertEquals("3800", cuentaCreada.getSaldo().toPlainString());
	}

	@Test
	@Order(5)
	void testEliminar() {
		ResponseEntity<Cuenta[]> respuesta = client.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
		List<Cuenta> cuentas = Arrays.asList(respuesta.getBody());
		assertEquals(3, cuentas.size());

		Map<String, Long> pathVariables = new HashMap<>();
		pathVariables.put("id", 3L);
		ResponseEntity<Void> exchange = client.exchange(crearUri("/api/cuentas/{id}"), HttpMethod.DELETE, null,
				Void.class, pathVariables);

		assertEquals(HttpStatus.NO_CONTENT, exchange.getStatusCode());
		assertFalse(exchange.hasBody());

		respuesta = client.getForEntity(crearUri("/api/cuentas"), Cuenta[].class);
		cuentas = Arrays.asList(respuesta.getBody());
		assertEquals(2, cuentas.size());

		ResponseEntity<Cuenta> respuestaDetalle = client.getForEntity(crearUri("/api/cuentas/3"), Cuenta.class);
		assertEquals(HttpStatus.NOT_FOUND, respuestaDetalle.getStatusCode());
		assertFalse(respuestaDetalle.hasBody());
	}

	private String crearUri(String uri) {
		return "http://localhost:" + puerto + uri;
	}

}

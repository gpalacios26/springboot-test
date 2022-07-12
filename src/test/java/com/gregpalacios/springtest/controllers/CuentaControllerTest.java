package com.gregpalacios.springtest.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gregpalacios.springtest.Datos;
import com.gregpalacios.springtest.models.Cuenta;
import com.gregpalacios.springtest.models.TransaccionDto;
import com.gregpalacios.springtest.services.CuentaService;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private CuentaService cuentaService;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
	}

	@Test
	void testDetalle() throws Exception {
		// Given
		when(cuentaService.findById(1L)).thenReturn(Datos.crearCuenta001().orElseThrow(null));

		// When
		mvc.perform(MockMvcRequestBuilders.get("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON))
				// Then
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.persona").value("Andrés")).andExpect(jsonPath("$.saldo").value("1000"));

		verify(cuentaService).findById(1L);
	}

	@Test
	void testTransferir() throws Exception {
		// Given
		TransaccionDto dto = new TransaccionDto();
		dto.setCuentaOrigenId(1L);
		dto.setCuentaDestinoId(2L);
		dto.setMonto(new BigDecimal("100"));
		dto.setBancoId(1L);

		Map<String, Object> response = new HashMap<>();
		response.put("date", LocalDate.now().toString());
		response.put("status", "OK");
		response.put("mensaje", "Transferencia realizada con éxito");
		response.put("transaccion", dto);

		// When
		mvc.perform(MockMvcRequestBuilders.post("/api/cuentas/transferir").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				// Then
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
				.andExpect(jsonPath("$.status").value("OK"))
				.andExpect(jsonPath("$.transaccion.cuentaOrigenId").value(dto.getCuentaOrigenId()))
				.andExpect(content().json(objectMapper.writeValueAsString(response)));
	}

	@Test
	void testListar() throws Exception {
		// Given
		List<Cuenta> cuentas = Arrays.asList(Datos.crearCuenta001().orElseThrow(null),
				Datos.crearCuenta002().orElseThrow(null));
		when(cuentaService.findAll()).thenReturn(cuentas);

		// When
		mvc.perform(MockMvcRequestBuilders.get("/api/cuentas").contentType(MediaType.APPLICATION_JSON))
				// Then
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].persona").value("Andrés")).andExpect(jsonPath("$[1].persona").value("Jhon"))
				.andExpect(jsonPath("$[0].saldo").value("1000")).andExpect(jsonPath("$[1].saldo").value("2000"))
				.andExpect(content().json(objectMapper.writeValueAsString(cuentas)));
	}

	@Test
	void testGuardar() throws Exception {
		// Given
		Cuenta cuenta = new Cuenta(null, "Pepe", new BigDecimal("3000"));
		when(cuentaService.save(any())).thenReturn(cuenta);

		// When
		mvc.perform(MockMvcRequestBuilders.post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cuenta)))
				// Then
				.andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json(objectMapper.writeValueAsString(cuenta)));

		verify(cuentaService).save(any());
	}

}

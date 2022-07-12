package com.gregpalacios.springtest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.gregpalacios.springtest.exceptions.DineroInsuficienteException;
import com.gregpalacios.springtest.models.Banco;
import com.gregpalacios.springtest.models.Cuenta;
import com.gregpalacios.springtest.repositories.BancoRepository;
import com.gregpalacios.springtest.repositories.CuentaRepository;
import com.gregpalacios.springtest.services.CuentaService;

@SpringBootTest
class SpringbootTestApplicationTests {

	@MockBean
	CuentaRepository cuentaRepo;

	@MockBean
	BancoRepository bancoRepo;

	@Autowired
	CuentaService service;

	@BeforeEach
	void setUp() {
		// cuentaRepo = mock(CuentaRepository.class);
		// bancoRepo = mock(BancoRepository.class);
		// service = new CuentaServiceImpl(cuentaRepo, bancoRepo);
	}

	@Test
	void contextLoads() {
		when(cuentaRepo.findById(1L)).thenReturn(Datos.crearCuenta001());
		when(cuentaRepo.findById(2L)).thenReturn(Datos.crearCuenta002());
		when(bancoRepo.findById(1L)).thenReturn(Datos.crearBanco());

		BigDecimal saldoOrigen = service.revisarSaldo(1L);
		BigDecimal saldoDestino = service.revisarSaldo(2L);

		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());

		service.transferir(1L, 2L, new BigDecimal("100"), 1L);

		saldoOrigen = service.revisarSaldo(1L);
		saldoDestino = service.revisarSaldo(2L);

		assertEquals("900", saldoOrigen.toPlainString());
		assertEquals("2100", saldoDestino.toPlainString());

		int total = service.revisarTotalTransferencias(1L);

		assertEquals(1, total);

		verify(cuentaRepo, times(3)).findById(1L);
		verify(cuentaRepo, times(3)).findById(2L);
		verify(cuentaRepo, times(2)).save(any(Cuenta.class));

		verify(bancoRepo, times(2)).findById(1L);
		verify(bancoRepo).save(any(Banco.class));

		verify(cuentaRepo, times(6)).findById(anyLong());
		verify(cuentaRepo, never()).findAll();
	}

	@Test
	void contextLoads2() {
		when(cuentaRepo.findById(1L)).thenReturn(Datos.crearCuenta001());
		when(cuentaRepo.findById(2L)).thenReturn(Datos.crearCuenta002());
		when(bancoRepo.findById(1L)).thenReturn(Datos.crearBanco());

		BigDecimal saldoOrigen = service.revisarSaldo(1L);
		BigDecimal saldoDestino = service.revisarSaldo(2L);

		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());

		assertThrows(DineroInsuficienteException.class, () -> {
			service.transferir(1L, 2L, new BigDecimal("1200"), 1L);
		});

		saldoOrigen = service.revisarSaldo(1L);
		saldoDestino = service.revisarSaldo(2L);

		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());

		int total = service.revisarTotalTransferencias(1L);

		assertEquals(0, total);

		verify(cuentaRepo, times(3)).findById(1L);
		verify(cuentaRepo, times(2)).findById(2L);
		verify(cuentaRepo, never()).save(any(Cuenta.class));

		verify(bancoRepo, times(1)).findById(1L);
		verify(bancoRepo, never()).save(any(Banco.class));

		verify(cuentaRepo, times(5)).findById(anyLong());
		verify(cuentaRepo, never()).findAll();
	}

	@Test
	void contextLoads3() {
		when(cuentaRepo.findById(1L)).thenReturn(Datos.crearCuenta001());

		Cuenta cuenta1 = service.findById(1L);
		Cuenta cuenta2 = service.findById(1L);

		assertSame(cuenta1, cuenta2);
		assertTrue(cuenta1 == cuenta2);
		assertEquals("Andrés", cuenta1.getPersona());
		assertEquals("Andrés", cuenta2.getPersona());

		verify(cuentaRepo, times(2)).findById(1L);
	}

	@Test
	void testFindAll() {
		// Given
		List<Cuenta> datos = Arrays.asList(Datos.crearCuenta001().orElseThrow(null),
				Datos.crearCuenta002().orElseThrow(null));
		when(cuentaRepo.findAll()).thenReturn(datos);

		// When
		List<Cuenta> cuentas = service.findAll();

		// Then
		assertFalse(cuentas.isEmpty());
		assertEquals(2, cuentas.size());
		assertTrue(cuentas.contains(Datos.crearCuenta002().orElseThrow(null)));

		verify(cuentaRepo).findAll();
	}

	@Test
	void testSave() {
		// Given
		Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
		when(cuentaRepo.save(any())).thenReturn(cuentaPepe);

		// When
		Cuenta cuenta = service.save(cuentaPepe);

		// Then
		assertEquals("Pepe", cuenta.getPersona());
		assertEquals("3000", cuenta.getSaldo().toPlainString());

		verify(cuentaRepo).save(any());
	}

}

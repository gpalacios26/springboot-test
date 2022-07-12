package com.gregpalacios.springtest;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.gregpalacios.springtest.models.Cuenta;
import com.gregpalacios.springtest.repositories.CuentaRepository;

@Tag("integracion_jpa")
@DataJpaTest
public class IntegracionJpaTest {

	@Autowired
	private CuentaRepository cuentaRepo;

	@Test
	void testFindById() {
		Optional<Cuenta> cuenta = cuentaRepo.findById(1L);

		assertTrue(cuenta.isPresent());
		assertEquals("Andrés", cuenta.orElseThrow(null).getPersona());
	}

	@Test
	void testFindByPersona() {
		Optional<Cuenta> cuenta = cuentaRepo.findByPersona("Andrés");

		assertTrue(cuenta.isPresent());
		assertEquals("Andrés", cuenta.orElseThrow(null).getPersona());
		assertEquals("1000.00", cuenta.orElseThrow(null).getSaldo().toPlainString());
	}

	@Test
	void testFindByPersonaThrowException() {
		Optional<Cuenta> cuenta = cuentaRepo.findByPersona("Rob");

		assertThrows(NullPointerException.class, () -> {
			cuenta.orElseThrow(null);
		});

		assertFalse(cuenta.isPresent());
	}

	@Test
	void testFindAll() {
		List<Cuenta> cuenta = cuentaRepo.findAll();

		assertFalse(cuenta.isEmpty());
		assertEquals(2, cuenta.size());
	}

	@Test
	void testSave() {
		// Given
		Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
		Cuenta cuenta = cuentaRepo.save(cuentaPepe);

		// When
		// Cuenta cuenta = cuentaRepo.findByPersona("Pepe").orElseThrow(null);

		// Then
		assertEquals("Pepe", cuenta.getPersona());
		assertEquals("3000", cuenta.getSaldo().toPlainString());
	}

	@Test
	void testUpdate() {
		// Given
		Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));

		// When
		Cuenta cuenta = cuentaRepo.save(cuentaPepe);

		// Then
		assertEquals("Pepe", cuenta.getPersona());
		assertEquals("3000", cuenta.getSaldo().toPlainString());

		// When
		cuenta.setSaldo(new BigDecimal("3800"));
		Cuenta cuentaActualizada = cuentaRepo.save(cuenta);

		// Then
		assertEquals("Pepe", cuentaActualizada.getPersona());
		assertEquals("3800", cuentaActualizada.getSaldo().toPlainString());
	}

	@Test
	void testDelete() {
		Cuenta cuenta = cuentaRepo.findById(2L).orElseThrow(null);
		assertEquals("John", cuenta.getPersona());

		cuentaRepo.delete(cuenta);

		assertThrows(NullPointerException.class, () -> {
			// cuentaRepo.findByPersona("John").orElseThrow(null);
			cuentaRepo.findById(2L).orElseThrow(null);
		});
	}

}

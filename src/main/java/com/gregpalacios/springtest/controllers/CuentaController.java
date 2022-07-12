package com.gregpalacios.springtest.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gregpalacios.springtest.models.Cuenta;
import com.gregpalacios.springtest.models.TransaccionDto;
import com.gregpalacios.springtest.services.CuentaService;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

	@Autowired
	private CuentaService cuentaService;

	@GetMapping()
	@ResponseStatus(HttpStatus.OK)
	public List<Cuenta> listar() {
		return cuentaService.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> detalle(@PathVariable("id") Long id) {
		Cuenta cuenta = null;
		try {
			cuenta = cuentaService.findById(id);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(cuenta);
	}

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public Cuenta guardar(@RequestBody Cuenta cuenta) {
		return cuentaService.save(cuenta);
	}

	@PostMapping("/transferir")
	public ResponseEntity<?> transferir(@RequestBody TransaccionDto dto) {
		cuentaService.transferir(dto.getCuentaOrigenId(), dto.getCuentaDestinoId(), dto.getMonto(), dto.getBancoId());

		Map<String, Object> response = new HashMap<>();
		response.put("date", LocalDate.now().toString());
		response.put("status", "OK");
		response.put("mensaje", "Transferencia realizada con éxito");
		response.put("transaccion", dto);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable("id") Long id) {
		cuentaService.deleteById(id);
	}

}

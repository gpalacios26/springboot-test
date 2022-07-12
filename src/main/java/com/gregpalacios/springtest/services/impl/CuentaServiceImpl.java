package com.gregpalacios.springtest.services.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gregpalacios.springtest.models.Banco;
import com.gregpalacios.springtest.models.Cuenta;
import com.gregpalacios.springtest.repositories.BancoRepository;
import com.gregpalacios.springtest.repositories.CuentaRepository;
import com.gregpalacios.springtest.services.CuentaService;

@Service
public class CuentaServiceImpl implements CuentaService {

	private CuentaRepository cuentaRepository;

	private BancoRepository bancoRepository;

	public CuentaServiceImpl(CuentaRepository cuentaRepository, BancoRepository bancoRepository) {
		this.cuentaRepository = cuentaRepository;
		this.bancoRepository = bancoRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Cuenta findById(Long id) {
		return cuentaRepository.findById(id).orElseThrow(null);
	}

	@Override
	@Transactional(readOnly = true)
	public int revisarTotalTransferencias(Long bancoId) {
		Banco banco = bancoRepository.findById(bancoId).orElseThrow(null);
		return banco.getTotalTransferencias();
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal revisarSaldo(Long cuentaId) {
		Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow(null);
		return cuenta.getSaldo();
	}

	@Override
	@Transactional()
	public void transferir(Long numCuentaOrigen, Long numCuentaDestino, BigDecimal monto, Long bancoId) {
		Cuenta cuentaOrigen = cuentaRepository.findById(numCuentaOrigen).orElseThrow(null);
		cuentaOrigen.debito(monto);
		cuentaRepository.save(cuentaOrigen);

		Cuenta cuentaDestino = cuentaRepository.findById(numCuentaDestino).orElseThrow(null);
		cuentaDestino.credito(monto);
		cuentaRepository.save(cuentaDestino);

		Banco banco = bancoRepository.findById(bancoId).orElseThrow(null);
		int totalTransferencias = banco.getTotalTransferencias();
		banco.setTotalTransferencias(++totalTransferencias);
		bancoRepository.save(banco);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Cuenta> findAll() {
		return cuentaRepository.findAll();
	}

	@Override
	@Transactional()
	public Cuenta save(Cuenta cuenta) {
		return cuentaRepository.save(cuenta);
	}

	@Override
	@Transactional()
	public void deleteById(Long id) {
		cuentaRepository.deleteById(id);
	}

}

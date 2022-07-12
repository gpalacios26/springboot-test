package com.gregpalacios.springtest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gregpalacios.springtest.models.Banco;

public interface BancoRepository extends JpaRepository<Banco, Long> {

}

package com.guardioes.propostas.web.model;

import com.guardioes.propostas.entity.Proposta;
import com.guardioes.propostas.web.dto.PropostaCreateDto;
import com.guardioes.propostas.web.dto.PropostaResponseDto;
import org.modelmapper.ModelMapper;

public class PropostaMapper {
    public static Proposta paraProposta(PropostaCreateDto dto) {
        Proposta proposta = new ModelMapper().map(dto, Proposta.class);
        proposta.setFuncionarioId(1L);
        return proposta;
    }

    public static PropostaResponseDto paraDto(Proposta proposta) {
        PropostaResponseDto dto = new ModelMapper().map(proposta, PropostaResponseDto.class);
        dto.setFuncionarioNome("Nome");
        dto.setFuncionarioCpf("Cpf");
        return dto;
    }
}

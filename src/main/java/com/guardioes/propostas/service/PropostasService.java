package com.guardioes.propostas.service;

import com.guardioes.propostas.client.funcionarios.Funcionario;
import com.guardioes.propostas.client.funcionarios.FuncionariosClient;
import com.guardioes.propostas.entity.Proposta;
import com.guardioes.propostas.entity.Votacao;
import com.guardioes.propostas.repository.PropostaRepository;
import com.guardioes.propostas.repository.VotacaoRepository;
import com.guardioes.propostas.web.dto.VotacaoDto;
import com.guardioes.propostas.web.dto.VotacaoInitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Timer;
import java.util.TimerTask;

@Service
@RequiredArgsConstructor
public class PropostasService {

    private final PropostaRepository propostaRepository;
    private final FuncionariosClient funcionariosClient;
    private final VotacaoRepository votacaoRepository;

    @Transactional
    public Proposta criar(Proposta proposta) {
        return propostaRepository.save(proposta);
    }

    public Proposta iniciarVotacao(VotacaoInitDto dto) {
        Proposta proposta = propostaRepository.findByTitulo(dto.getPropostaTitulo())
                .orElseThrow(() -> new RuntimeException("Proposta not found"));

        proposta.setAtivo(true);
        proposta.setTempo(dto.getTempo());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                proposta.setAtivo(false);
                propostaRepository.save(proposta);
            }
        }, dto.getTempo() * 60000L);

        return propostaRepository.save(proposta);
    }

    @Transactional
    public Proposta votar(VotacaoDto dto) {
        Proposta proposta = propostaRepository.findByTitulo(dto.getTitulo())
                .orElseThrow(() -> new RuntimeException("Proposta not found"));

        if (!proposta.isAtivo()) {
            throw new RuntimeException("A proposta não está ativa para votação");
        }

        Funcionario funcionario = funcionariosClient.getFuncionarioByCpf(dto.getCpf());
        if (funcionario == null) {
            throw new RuntimeException("Funcionário não encontrado");
        }

        boolean cpfJaVotou = votacaoRepository.existsByTituloAndFuncionarioCpf(dto.getTitulo(),dto.getCpf());
        if (cpfJaVotou) {
            throw new RuntimeException("Este CPF já votou nesta proposta");
        }

        Votacao voto = new Votacao();
        voto.setTitulo(dto.getTitulo());
        voto.setIdProposta(propostaRepository.findByTitulo(dto.getTitulo()).get().getId());
        voto.setFuncionarioCpf(dto.getCpf());
        voto.setVoto(dto.getStatusVoto());
        votacaoRepository.save(voto);

        if (dto.getStatusVoto() == Votacao.StatusVoto.APROVAR) {
            proposta.setAprovar(proposta.getAprovar() + 1);
        } else if (dto.getStatusVoto() == Votacao.StatusVoto.REJEITAR) {
            proposta.setRejeitar(proposta.getRejeitar() + 1);
        } else {
            throw new RuntimeException("Invalid vote type");
        }

        return propostaRepository.save(proposta);
    }
}
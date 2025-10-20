package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.AvaliacaoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepositorio avaliacaoRepositorio;

    public Avaliacao salvarAvaliacao(Avaliacao avaliacao) {
        if (avaliacao.getData() == null) {
            avaliacao.setData(LocalDate.now());
        }

        if (avaliacao.getNota() < 0 || avaliacao.getNota() > 10) {
            throw new IllegalArgumentException("A nota deve estar entre 0 e 10.");
        }

        return avaliacaoRepositorio.save(avaliacao);
    }

    //Daqui pra baixo é só para admim
    public List<Avaliacao> listarAvaliacoes() {
        return avaliacaoRepositorio.findAll();
    }

    public Optional<Avaliacao> buscarPorId(String id) {
        return avaliacaoRepositorio.findById(id);
    }

    public void deletarAvaliacao(String id) {
        if (!avaliacaoRepositorio.existsById(id)) {
            throw new RuntimeException("Avaliação não encontrada para exclusão.");
        }
        avaliacaoRepositorio.deleteById(id);
    }
}

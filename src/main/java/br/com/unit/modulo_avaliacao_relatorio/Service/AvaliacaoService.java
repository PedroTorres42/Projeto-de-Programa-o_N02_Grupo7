package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Nota;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.AvaliacaoRepositorio;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.NotaRespositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepositorio avaliacaoRepositorio;
    private final NotaRespositorio notaRespositorio;

    public Avaliacao salvarAvaliacao(Long avaliacaoId) {
        Avaliacao avaliacao = avaliacaoRepositorio.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
        if (avaliacao.getData() == null) {
            avaliacao.setData(LocalDate.now());
        }

        if (avaliacao.getNota() < 0 || avaliacao.getNota() > 10) {
            throw new IllegalArgumentException("A nota deve estar entre 0 e 10.");
                    .mapToDouble(Nota::getNota)
                    .average()
                    .orElse(0.0);
            avaliacao.setMedia(mediaCalculada);
        }

        if (avaliacao.getNotas() != null) {
            avaliacao.getNotas().forEach(n -> n.setAvaliacao(avaliacao));
        }

       
        if (avaliacao.getFeedback() != null) {
            Feedback fb = avaliacao.getFeedback();
            fb.setAvaliacao(avaliacao);
        }

        if (avaliacao.getMedia() != null && (avaliacao.getMedia() < 0 || avaliacao.getMedia() > 10)) {
            throw new IllegalArgumentException("A média deve ser entre 0 e 10.");

        }

        return avaliacaoRepositorio.save(avaliacao);
    }

    public List<Avaliacao> listarAvaliacoes() {
        return avaliacaoRepositorio.findAll();
    }

    public Avaliacao buscarPorId(Long id) {
        return avaliacaoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada para ID: " + id));
    }

    public void deletarAvaliacao(Long id) {
        Avaliacao avaliacao = buscarPorId(id);
        avaliacaoRepositorio.delete(avaliacao);
    }
}

package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
<<<<<<< Updated upstream
=======
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Feedback;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Nota;
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream

    public Avaliacao salvarAvaliacao(Avaliacao avaliacao) {
=======
    private final NotaRespositorio notaRespositorio;

    /**
     * Salva uma avaliação já montada (vinda da UI).
     * - Se vier com notas, recalcula a média.
     * - Garante data preenchida.
     * - Garante que Feedback exista (se vier nulo e houver comentário).
     */
    public Avaliacao salvarAvaliacao(Avaliacao avaliacao) {
        if (avaliacao == null) {
            throw new IllegalArgumentException("Avaliação não pode ser nula.");
        }

        // Data
>>>>>>> Stashed changes
        if (avaliacao.getData() == null) {
            avaliacao.setData(LocalDate.now());
        }

<<<<<<< Updated upstream
        if (avaliacao.getNota() < 0 || avaliacao.getNota() > 10) {
            throw new IllegalArgumentException("A nota deve estar entre 0 e 10.");
=======
        // Se veio com notas, calcula média a partir delas; senão, mantém a que veio do formulário
        if (avaliacao.getNotas() != null && !avaliacao.getNotas().isEmpty()) {
            double mediaCalculada = avaliacao.getNotas().stream()
                    .filter(n -> n.getNota() != null)
                    .mapToDouble(Nota::getNota)
                    .average()
                    .orElse(0.0);
            avaliacao.setMedia(mediaCalculada);
        }

        // Garante vínculo bidirecional das notas (se existirem)
        if (avaliacao.getNotas() != null) {
            avaliacao.getNotas().forEach(n -> n.setAvaliacao(avaliacao));
        }

        // Feedback: se vier null mas tivermos algo para comentar futuramente, criamos;
        // aqui apenas garantimos não NPE. A UI já está montando Feedback, mas deixamos robusto.
        if (avaliacao.getFeedback() != null) {
            Feedback fb = avaliacao.getFeedback();
            fb.setAvaliacao(avaliacao);
        }

        // Regras simples (mantendo seu intervalo 0..10 para média, se isso for regra do domínio)
        if (avaliacao.getMedia() != null && (avaliacao.getMedia() < 0 || avaliacao.getMedia() > 10)) {
            throw new IllegalArgumentException("A média deve ser entre 0 e 10.");
>>>>>>> Stashed changes
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

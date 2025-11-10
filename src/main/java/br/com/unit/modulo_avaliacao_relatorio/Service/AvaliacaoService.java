package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Feedback;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Nota;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.AvaliacaoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepositorio avaliacaoRepositorio;

    @Transactional
    public Avaliacao salvarAvaliacao(Avaliacao avaliacao) {
        if (avaliacao == null) throw new IllegalArgumentException("Avaliação inválida");

        if (avaliacao.getNotas() != null) {
            avaliacao.getNotas().forEach(n -> {
                if (n != null) n.setAvaliacao(avaliacao);
            });
        }

        if (avaliacao.getFeedback() != null) {
            Feedback fb = avaliacao.getFeedback();
            fb.setAvaliacao(avaliacao);
        }

        Double media = calcularMedia(avaliacao);
        double maxPermitido = (avaliacao.getNotas() == null || avaliacao.getNotas().isEmpty()) ? 10.0 : 5.0;
        if (media < 0.0 || media > maxPermitido) {
            String msg = maxPermitido == 10.0 ? "A média deve estar entre 0 e 10." : "A média deve estar entre 0 e 5.";
            throw new IllegalArgumentException(msg);
        }
        avaliacao.setMedia(media);

        return avaliacaoRepositorio.save(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarAvaliacoes() {
        return avaliacaoRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Avaliacao buscarPorId(Long id) {
        return avaliacaoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada para ID: " + id));
    }

    @Transactional
    public Avaliacao atualizarAvaliacao(Long id, Avaliacao dados) {
        Avaliacao existente = buscarPorId(id);

        if (dados.getAluno() != null) existente.setAluno(dados.getAluno());
        if (dados.getInstrutor() != null) existente.setInstrutor(dados.getInstrutor());
        if (dados.getCurso() != null) existente.setCurso(dados.getCurso());
        if (dados.getFormulario() != null) existente.setFormulario(dados.getFormulario());

        if (dados.getFeedback() != null) {
            Feedback fb = dados.getFeedback();
            fb.setAvaliacao(existente);
            existente.setFeedback(fb);
        }

        if (dados.getNotas() != null) {
            existente.setNotas(dados.getNotas());
            existente.getNotas().stream().filter(Objects::nonNull).forEach(n -> n.setAvaliacao(existente));
        }

        Double media = calcularMedia(existente);
        double maxPermitido = (existente.getNotas() == null || existente.getNotas().isEmpty()) ? 10.0 : 5.0;
        if (media < 0.0 || media > maxPermitido) {
            String msg = maxPermitido == 10.0 ? "A média deve estar entre 0 e 10." : "A média deve estar entre 0 e 5.";
            throw new IllegalArgumentException(msg);
        }
        existente.setMedia(media);

        return avaliacaoRepositorio.save(existente);
    }

    @Transactional
    public void deletarAvaliacao(Long id) {
        Avaliacao avaliacao = buscarPorId(id);
        avaliacaoRepositorio.delete(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorCurso(Long cursoId) {
        return avaliacaoRepositorio.findByCursoId(cursoId);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorInstrutor(String instrutorId) {
        return avaliacaoRepositorio.findByInstrutorId(instrutorId);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorAluno(String alunoId) {
        return avaliacaoRepositorio.findByAlunoId(alunoId);
    }

    private Double calcularMedia(Avaliacao a) {
        if (a.getNotas() != null && !a.getNotas().isEmpty()) {
            double soma = 0.0;
            int cont = 0;
            for (Nota n : a.getNotas()) {
                if (n != null && n.getNota() != null) {
                    // Ignora notas de perguntas do tipo FREQUENCIA no cálculo da média de 1..5
                    Pergunta p = n.getPergunta();
                    if (p != null && p.getTipo() == Pergunta.TipoPergunta.FREQUENCIA) {
                        continue;
                    }
                    soma += n.getNota();
                    cont++;
                }
            }
            if (cont > 0) return soma / cont;
        }
        return a.getMedia() != null ? a.getMedia() : 0.0;
    }
}

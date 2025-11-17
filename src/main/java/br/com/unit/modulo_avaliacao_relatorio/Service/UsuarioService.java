package br.com.unit.modulo_avaliacao_relatorio.Service;


import br.com.unit.modulo_avaliacao_relatorio.Modelos.Aluno;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Instrutor;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Usuario;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UsuarioService {


    private final UsuarioRepositorio usuarioRepositorio;


    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        return usuarioRepositorio.save(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return usuarioRepositorio.findByEmail(email);
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) return Optional.empty();
        return usuarioRepositorio.findAll().stream()
            .filter(u -> u instanceof Aluno)
            .map(u -> (Aluno) u)
            .filter(a -> matricula.equals(a.getMatricula()))
            .map(a -> (Usuario) a)
            .findFirst();
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmailOuMatricula(String identificador) {
        if (identificador == null || identificador.isBlank()) return Optional.empty();
        
        Optional<Usuario> usuario = buscarPorEmail(identificador);
        
        if (usuario.isEmpty() && !identificador.contains("@")) {
            usuario = buscarPorMatricula(identificador);
        }
        
        return usuario;
    }


    @Transactional
    public void deletarUsuario(String id) {
        usuarioRepositorio.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Aluno> listarAlunos() {
    return usuarioRepositorio.findAll().stream()
        .filter(u -> u instanceof Aluno)
        .map(u -> (Aluno) u)
        .collect(Collectors.toList());
}

@Transactional(readOnly = true)
public List<Instrutor> listarInstrutores() {
    return usuarioRepositorio.findAll().stream()
        .filter(u -> u instanceof Instrutor)
        .map(u -> (Instrutor) u)
        .collect(Collectors.toList());
}
}

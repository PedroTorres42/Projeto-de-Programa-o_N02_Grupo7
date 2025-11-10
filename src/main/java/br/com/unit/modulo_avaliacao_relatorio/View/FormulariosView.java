package br.com.unit.modulo_avaliacao_relatorio.View;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Formulario;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Pergunta;
import br.com.unit.modulo_avaliacao_relatorio.Service.FormularioService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

@Lazy
@Component
public class FormulariosView extends JFrame {
    
    private final FormularioService formularioService;
    
    private JTable tabelaFormularios;
    private DefaultTableModel modeloTabela;
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnExcluir;
    private JButton btnVisualizar;
    private JButton btnAtualizar;
    
    private final List<Formulario> formulariosCarregados = new ArrayList<>();
    private boolean loading = false;
    
    public FormulariosView(FormularioService formularioService) {
        this.formularioService = formularioService;
        setTitle("Gerenciar Formulários");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    UIUtils.padronizarJanela(this);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                carregarFormularios();
            }
        });
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel lblTitulo = new JLabel("Gerenciamento de Formulários");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        painelSuperior.add(lblTitulo);
        
    btnAtualizar = UIUtils.primaryButton("Atualizar", this::carregarFormularios);
        btnAtualizar.setToolTipText("Recarregar lista de formulários");
        painelSuperior.add(btnAtualizar);
        
        add(painelSuperior, BorderLayout.NORTH);
        
        String[] colunas = {"ID", "Título", "Tipo", "Nº Perguntas"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaFormularios = new JTable(modeloTabela);
        tabelaFormularios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaFormularios.setFillsViewportHeight(true);
        tabelaFormularios.getTableHeader().setReorderingAllowed(false);
        
        tabelaFormularios.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaFormularios.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabelaFormularios.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabelaFormularios.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(tabelaFormularios);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        painelInferior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
    JButton btnVoltar = UIUtils.dangerButton("Voltar", this::dispose);
    btnVoltar.setToolTipText("Fechar esta janela e retornar ao menu");
        painelInferior.add(btnVoltar);
        
    btnVisualizar = UIUtils.primaryButton("Visualizar", this::visualizarFormulario);
        painelInferior.add(btnVisualizar);
        
    btnNovo = UIUtils.successButton("Novo Formulário", this::novoFormulario);
        painelInferior.add(btnNovo);
        
    btnEditar = UIUtils.warningButton("Editar", this::editarFormulario);
        painelInferior.add(btnEditar);
        
    btnExcluir = UIUtils.dangerButton("Excluir", this::excluirFormulario);
        painelInferior.add(btnExcluir);
        
        add(painelInferior, BorderLayout.SOUTH);
        
        tabelaFormularios.getSelectionModel().addListSelectionListener(e -> {
            boolean temSelecao = tabelaFormularios.getSelectedRow() != -1;
            boolean enable = temSelecao && !loading;
            btnVisualizar.setEnabled(enable);
            btnEditar.setEnabled(enable);
            btnExcluir.setEnabled(enable);
        });
        
        btnVisualizar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false);
    }
    
    private void setLoading(boolean value) {
        loading = value;
        setCursor(value ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        btnAtualizar.setEnabled(!value);
        btnNovo.setEnabled(!value);
        tabelaFormularios.setEnabled(!value);
    }
    
    private void carregarFormularios() {
        SwingWorker<List<Formulario>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Formulario> doInBackground() {
                setLoading(true);
                return formularioService.listarFormularios();
            }
            
            @Override
            protected void done() {
                try {
                    formulariosCarregados.clear();
                    modeloTabela.setRowCount(0);
                    
                    List<Formulario> formularios = get();
                    formulariosCarregados.addAll(formularios);
                    
                    for (Formulario f : formularios) {
                        Object[] linha = {
                            f.getId(),
                            f.getTitulo(),
                            f.getTipo() != null ? f.getTipo().name() : "N/A",
                            f.getPerguntas() != null ? f.getPerguntas().size() : 0
                        };
                        modeloTabela.addRow(linha);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(FormulariosView.this,
                        "Erro ao carregar formulários: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setLoading(false);
                }
            }
        };
        worker.execute();
    }
    
    private void visualizarFormulario() {
        int selectedRow = tabelaFormularios.getSelectedRow();
        if (selectedRow == -1) return;
        
        Formulario formulario = formulariosCarregados.get(selectedRow);
        
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(formulario.getId()).append("\n");
        sb.append("Título: ").append(formulario.getTitulo()).append("\n");
        sb.append("Tipo: ").append(formulario.getTipo() != null ? formulario.getTipo().name() : "N/A").append("\n");
        sb.append("\nPerguntas:\n");
        
        if (formulario.getPerguntas() != null && !formulario.getPerguntas().isEmpty()) {
            int i = 1;
            for (Pergunta p : formulario.getPerguntas()) {
                sb.append(i++).append(". ").append(p.getTexto());
                sb.append(" (Tipo: ").append(p.getTipo()).append(")\n");
            }
        } else {
            sb.append("Nenhuma pergunta cadastrada.\n");
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Detalhes do Formulário", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void novoFormulario() {
        JDialog dialog = new JDialog(this, "Novo Formulário", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel painelCabecalho = new JPanel(new GridLayout(2, 2, 10, 10));
        painelCabecalho.add(new JLabel("Título:"));
        JTextField txtTitulo = new JTextField();
        painelCabecalho.add(txtTitulo);
        
        painelCabecalho.add(new JLabel("Tipo:"));
        JComboBox<Formulario.TipoFormulario> comboTipo = new JComboBox<>(Formulario.TipoFormulario.values());
        painelCabecalho.add(comboTipo);
        
        painelPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        
        DefaultListModel<String> modeloPerguntas = new DefaultListModel<>();
        JList<String> listaPerguntas = new JList<>(modeloPerguntas);
        listaPerguntas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPerguntas = new JScrollPane(listaPerguntas);
        scrollPerguntas.setBorder(BorderFactory.createTitledBorder("Perguntas"));
        painelPrincipal.add(scrollPerguntas, BorderLayout.CENTER);
        
        JPanel painelPerguntas = new JPanel(new BorderLayout(5, 5));
        JPanel painelBotoesPerguntas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAdicionarPergunta = new JButton("Adicionar Pergunta");
        JButton btnRemoverPergunta = new JButton("Remover Selecionada");
        
        List<PerguntaTemp> perguntasTemp = new ArrayList<>();
        
        btnAdicionarPergunta.addActionListener(e -> {
            String texto = JOptionPane.showInputDialog(dialog, "Texto da pergunta:");
            if (texto != null && !texto.trim().isEmpty()) {
                Pergunta.TipoPergunta[] tipos = Pergunta.TipoPergunta.values();
                Pergunta.TipoPergunta tipo = (Pergunta.TipoPergunta) JOptionPane.showInputDialog(
                    dialog, "Tipo da pergunta:", "Tipo",
                    JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);
                
                if (tipo != null) {
                    PerguntaTemp pt = new PerguntaTemp(texto, tipo);
                    perguntasTemp.add(pt);
                    modeloPerguntas.addElement(texto + " (" + tipo.name() + ")");
                }
            }
        });
        
        btnRemoverPergunta.addActionListener(e -> {
            int idx = listaPerguntas.getSelectedIndex();
            if (idx != -1) {
                perguntasTemp.remove(idx);
                modeloPerguntas.remove(idx);
            }
        });
        
        painelBotoesPerguntas.add(btnAdicionarPergunta);
        painelBotoesPerguntas.add(btnRemoverPergunta);
        painelPerguntas.add(painelBotoesPerguntas, BorderLayout.CENTER);
        painelPrincipal.add(painelPerguntas, BorderLayout.SOUTH);
        
        dialog.add(painelPrincipal, BorderLayout.CENTER);
        
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnSalvar.addActionListener(e -> {
            String titulo = txtTitulo.getText().trim();
            if (titulo.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Título é obrigatório!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (perguntasTemp.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Adicione pelo menos uma pergunta!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                List<Pergunta> perguntas = new ArrayList<>();
                for (PerguntaTemp pt : perguntasTemp) {
                    Pergunta p = new Pergunta();
                    p.setTexto(pt.texto);
                    p.setTipo(pt.tipo);
                    perguntas.add(p);
                }

                Formulario novoForm = new Formulario();
                novoForm.setTitulo(titulo);
                novoForm.setTipo((Formulario.TipoFormulario) comboTipo.getSelectedItem());
                novoForm.setPerguntas(perguntas);
                
                for (Pergunta p : perguntas) {
                    p.setFormulario(novoForm);
                }
                
                Formulario.TipoFormulario tipoSelecionado = (Formulario.TipoFormulario) comboTipo.getSelectedItem();
                formularioService.criarFormulario(titulo, perguntas, tipoSelecionado);
                
                JOptionPane.showMessageDialog(dialog, "Formulário criado com sucesso!");
                dialog.dispose();
                carregarFormularios();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erro ao criar formulário: " + ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        dialog.add(painelBotoes, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void editarFormulario() {
        int selectedRow = tabelaFormularios.getSelectedRow();
        if (selectedRow == -1) return;
        
        Formulario formulario = formulariosCarregados.get(selectedRow);
        
        JDialog dialog = new JDialog(this, "Editar Formulário", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel painelCabecalho = new JPanel(new GridLayout(2, 2, 10, 10));
        painelCabecalho.add(new JLabel("Título:"));
        JTextField txtTitulo = new JTextField(formulario.getTitulo());
        painelCabecalho.add(txtTitulo);
        
        painelCabecalho.add(new JLabel("Tipo:"));
        JComboBox<Formulario.TipoFormulario> comboTipo = new JComboBox<>(Formulario.TipoFormulario.values());
        comboTipo.setSelectedItem(formulario.getTipo());
        painelCabecalho.add(comboTipo);
        
        painelPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        
        DefaultListModel<String> modeloPerguntas = new DefaultListModel<>();
        JList<String> listaPerguntas = new JList<>(modeloPerguntas);
        listaPerguntas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPerguntas = new JScrollPane(listaPerguntas);
        scrollPerguntas.setBorder(BorderFactory.createTitledBorder("Perguntas"));
        painelPrincipal.add(scrollPerguntas, BorderLayout.CENTER);
        
        List<PerguntaTemp> perguntasTemp = new ArrayList<>();
        if (formulario.getPerguntas() != null) {
            for (Pergunta p : formulario.getPerguntas()) {
                PerguntaTemp pt = new PerguntaTemp(p.getTexto(), p.getTipo());
                perguntasTemp.add(pt);
                modeloPerguntas.addElement(p.getTexto() + " (" + p.getTipo().name() + ")");
            }
        }
        
        JPanel painelPerguntas = new JPanel(new BorderLayout(5, 5));
        JPanel painelBotoesPerguntas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAdicionarPergunta = new JButton("Adicionar Pergunta");
        JButton btnRemoverPergunta = new JButton("Remover Selecionada");
        
        btnAdicionarPergunta.addActionListener(e -> {
            String texto = JOptionPane.showInputDialog(dialog, "Texto da pergunta:");
            if (texto != null && !texto.trim().isEmpty()) {
                Pergunta.TipoPergunta[] tipos = Pergunta.TipoPergunta.values();
                Pergunta.TipoPergunta tipo = (Pergunta.TipoPergunta) JOptionPane.showInputDialog(
                    dialog, "Tipo da pergunta:", "Tipo",
                    JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);
                
                if (tipo != null) {
                    PerguntaTemp pt = new PerguntaTemp(texto, tipo);
                    perguntasTemp.add(pt);
                    modeloPerguntas.addElement(texto + " (" + tipo.name() + ")");
                }
            }
        });
        
        btnRemoverPergunta.addActionListener(e -> {
            int idx = listaPerguntas.getSelectedIndex();
            if (idx != -1) {
                perguntasTemp.remove(idx);
                modeloPerguntas.remove(idx);
            }
        });
        
        painelBotoesPerguntas.add(btnAdicionarPergunta);
        painelBotoesPerguntas.add(btnRemoverPergunta);
        painelPerguntas.add(painelBotoesPerguntas, BorderLayout.CENTER);
        painelPrincipal.add(painelPerguntas, BorderLayout.SOUTH);
        
        dialog.add(painelPrincipal, BorderLayout.CENTER);
        
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnSalvar.addActionListener(e -> {
            String titulo = txtTitulo.getText().trim();
            if (titulo.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Título é obrigatório!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (perguntasTemp.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Adicione pelo menos uma pergunta!", 
                    "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                List<Pergunta> perguntas = new ArrayList<>();
                for (PerguntaTemp pt : perguntasTemp) {
                    Pergunta p = new Pergunta();
                    p.setTexto(pt.texto);
                    p.setTipo(pt.tipo);
                    perguntas.add(p);
                }

                Formulario.TipoFormulario tipoSelecionado = (Formulario.TipoFormulario) comboTipo.getSelectedItem();
                formularioService.editarFormulario(formulario.getId(), perguntas, titulo, tipoSelecionado);
                
                JOptionPane.showMessageDialog(dialog, "Formulário atualizado com sucesso!");
                dialog.dispose();
                carregarFormularios();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erro ao atualizar formulário: " + ex.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        dialog.add(painelBotoes, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void excluirFormulario() {
        int selectedRow = tabelaFormularios.getSelectedRow();
        if (selectedRow == -1) return;
        
        Formulario formulario = formulariosCarregados.get(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Tem certeza que deseja excluir o formulário \"" + formulario.getTitulo() + "\"?\n" +
            "Esta ação não pode ser desfeita!",
            "Confirmar Exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                formularioService.deletarFormulario(formulario.getId());
                JOptionPane.showMessageDialog(this, "Formulário excluído com sucesso!");
                carregarFormularios();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao excluir formulário: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static class PerguntaTemp {
        String texto;
        Pergunta.TipoPergunta tipo;
        
        PerguntaTemp(String texto, Pergunta.TipoPergunta tipo) {
            this.texto = texto;
            this.tipo = tipo;
        }
    }
}

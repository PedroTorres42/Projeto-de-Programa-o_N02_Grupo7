package br.com.unit.modulo_avaliacao_relatorio.View;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;


public final class UIUtils {
    private UIUtils() {}

    public static JButton primaryButton(String text, Runnable onClick) {
        JButton b = new JButton(text);
        styleButton(b, UIConstants.PRIMARY, UIConstants.PRIMARY_HOVER);
        if (onClick != null) b.addActionListener(e -> onClick.run());
        return b;
    }

    public static JButton successButton(String text, Runnable onClick) {
        JButton b = new JButton(text);
        styleButton(b, UIConstants.SUCCESS, UIConstants.SUCCESS_HOVER);
        if (onClick != null) b.addActionListener(e -> onClick.run());
        return b;
    }

    public static JButton dangerButton(String text, Runnable onClick) {
        JButton b = new JButton(text);
        styleButton(b, UIConstants.DANGER, UIConstants.DANGER.darker());
        if (onClick != null) b.addActionListener(e -> onClick.run());
        return b;
    }

    public static JButton warningButton(String text, Runnable onClick) {
        JButton b = new JButton(text);
        styleButton(b, UIConstants.WARNING, UIConstants.WARNING.darker());
        if (onClick != null) b.addActionListener(e -> onClick.run());
        return b;
    }

    public static void styleButton(JButton b, Color normal, Color hover) {
        b.setFont(UIConstants.BUTTON);
        b.setBackground(normal);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { b.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent evt) { b.setBackground(normal); }
        });
    }

    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.TITLE);
        l.setForeground(UIConstants.TEXT_PRIMARY);
        return l;
    }

    public static JLabel subtitleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.SUBTITLE);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    public static JPanel verticalBox(Consumer<JPanel> builder) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIConstants.BG);
        if (builder != null) builder.accept(p);
        return p;
    }

    public static JPanel flowRight(Consumer<JPanel> builder) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(UIConstants.BG);
        if (builder != null) builder.accept(p);
        return p;
    }

    public static JPanel paddedBorderLayout(int pad) {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createEmptyBorder(pad,pad,pad,pad));
        p.setBackground(UIConstants.BG);
        return p;
    }


    public static void padronizarJanela(JFrame frame) {
        if (frame == null) return;
        frame.setMinimumSize(new Dimension(900, 600));
        Dimension cur = frame.getSize();
        if (cur.width < 900 || cur.height < 600) {
            frame.setSize(Math.max(cur.width, 900), Math.max(cur.height, 600));
        }
        frame.getContentPane().setBackground(UIConstants.BG);
    }
}

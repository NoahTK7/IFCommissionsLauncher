package com.noahkurrack.IFCommissionsLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

public class LauncherGUI extends JFrame {

    private static final int width = 625;
    private static final int height = 350;

    final private JPanel panel;
    private JTextArea console;
    private JScrollPane scrollPane;

    public LauncherGUI() {
        panel = new JPanel(true);

        console = new JTextArea(18, 45);
        scrollPane = new JScrollPane(console);

        PrintStream con = new PrintStream(new TextAreaOutputStream(console));
        System.setOut(con);
        System.setErr(con);

        panel.add(scrollPane);
        this.add(panel, BorderLayout.CENTER);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //console.setMinimumSize(new Dimension(width, height));

        init();
    }

    private void init() {
        setTitle("Invisible Fence Commissions Calculator Launcher");
        setSize(width,height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.out.println("Closing IFCommissions process if still running...");
                Main.getProcess().destroyForcibly();
                try {
                    Main.getProcess().waitFor();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println("Launcher exiting...");
            }
        });
    }

    public void close() {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}

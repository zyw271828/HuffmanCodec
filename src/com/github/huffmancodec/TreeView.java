package com.github.huffmancodec;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TreeView {

	private JFrame frame;
	private JScrollPane treeScrollPane;

	public TreeView(Process p) {
		initialize(p);
		frame.setVisible(true);
		frame.toFront();
		frame.repaint();
		frame.setLocationRelativeTo(null);
	}

	private void initialize(Process p) {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setBounds(100, 100, 800, 600);
		frame.setTitle("霍夫曼树 (换行符: ¶ 空格: · 制表符: »)");
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBackground(Color.WHITE);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
		frame.getContentPane().add(textArea, BorderLayout.CENTER);

		treeScrollPane = new JScrollPane(textArea);
		treeScrollPane.setBounds(textArea.getBounds());
		frame.getContentPane().add(treeScrollPane);

		try {
			WindowView.outputToTextArea("", p, textArea);
		} catch (IOException e) {
			e.printStackTrace();
		}

		textArea.setCaretPosition(0);
	}

}

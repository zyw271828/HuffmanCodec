package com.github.huffmancodec;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.iharder.dnd.FileDrop;

public class WindowView {

	private JFrame frame;
	private JScrollPane inputScrollPane;
	private JScrollPane outputScrollPane;

	private static boolean firstClick = true;
	private static String dndPath;
	private static String[] shell = new String[2];

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WindowView window = new WindowView();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public WindowView() {
		initialize();
		frame.setLocationRelativeTo(null);
	}

	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("霍夫曼编译码器");
		frame.setBounds(100, 100, 750, 400);
		frame.getContentPane().setBackground(new Color(0, 145, 234));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		// 判断操作系统
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		if (isWindows) {
			shell[0] = "cmd.exe";
			shell[1] = "/c";
		} else {
			shell[0] = "bash";
			shell[1] = "-c";
		}

		// 删除上次运行产生的临时文件
		try {
			Files.deleteIfExists(Paths.get("file/CodeFile.bin"));
			Files.deleteIfExists(Paths.get("file/TextFile.txt"));
			Files.deleteIfExists(Paths.get("file/Tree.bin"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JTextArea inputTextArea = new JTextArea();
		inputTextArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// 清空 inputTextArea 中的提示信息
				if (firstClick) {
					inputTextArea.setText("");
					firstClick = false;
				}
			}
		});
		inputTextArea.setText("将文件拖入此处自动编码\n或手动输入后点击编码按钮");
		inputTextArea.setToolTipText("将文件拖入此处自动编码；或手动输入后点击编码按钮");
		inputTextArea.setLineWrap(true);
		inputTextArea.setBounds(20, 20, 270, 330);
		inputTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));
		frame.getContentPane().add(inputTextArea);

		inputScrollPane = new JScrollPane(inputTextArea);
		inputScrollPane.setBounds(inputTextArea.getBounds());
		frame.getContentPane().add(inputScrollPane);

		JTextArea outputTextArea = new JTextArea();
		outputTextArea.setText("将二进制文件拖入此处自动解码");
		outputTextArea.setToolTipText("将二进制文件拖入此处自动解码");
		outputTextArea.setLineWrap(true);
		outputTextArea.setEditable(false);
		outputTextArea.setBounds(460, 20, 270, 330);
		outputTextArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));
		frame.getContentPane().add(outputTextArea);

		outputScrollPane = new JScrollPane(outputTextArea);
		outputScrollPane.setBounds(outputTextArea.getBounds());
		frame.getContentPane().add(outputScrollPane);

		// 文件拖入 inputTextArea 自动编码
		new FileDrop(inputTextArea, new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				firstClick = false;
				try {
					dndPath = files[0].getCanonicalPath();
					Process p = Runtime.getRuntime().exec(new String[] { shell[0], shell[1], "cd bin "
							+ "&& java -cp ../lib/algs4.jar:. com.github.huffmancodec.Huffman - < " + dndPath
							+ " > ../file/CodeFile.bin "
							+ "&& java -cp ../lib/algs4.jar:. com.github.huffmancodec.BinaryDump 20 < ../file/CodeFile.bin" });
					outputToTextArea("编码结果: \n", p, outputTextArea);
					if (outputTextArea.getText().equals("")) {
						JOptionPane.showConfirmDialog(frame, "编码失败", "错误", JOptionPane.PLAIN_MESSAGE);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// 文件拖入 outputTextArea 自动解码
		new FileDrop(outputTextArea, new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				firstClick = false;
				try {
					dndPath = files[0].getCanonicalPath();
					Process p = Runtime.getRuntime().exec(new String[] { shell[0], shell[1], "cd bin "
							+ "&& java -cp ../lib/algs4.jar:. com.github.huffmancodec.Huffman + < " + dndPath });
					outputToTextArea("", p, inputTextArea);
					if (inputTextArea.getText().equals("")) {
						JOptionPane.showConfirmDialog(frame, "解码失败", "错误", JOptionPane.PLAIN_MESSAGE);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		JButton encodeButton = new JButton("-> 编码 ->");
		encodeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				firstClick = false;
				if (inputTextArea.getText().equals("")) {
					// inputTextArea 为空
					outputTextArea.setText("编码结果: \n长度: 0 比特");
				} else {
					// inputTextArea 非空
					outputTextArea.setText("");
					String text = inputTextArea.getText();
					try {
						Process p = Runtime.getRuntime().exec(new String[] { shell[0], shell[1], "cd bin "
								+ "&& echo \"" + text + "\" > ../file/TextFile.txt "
								+ "&& java -cp ../lib/algs4.jar:. com.github.huffmancodec.Huffman - < ../file/TextFile.txt > ../file/CodeFile.bin "
								+ "&& java -cp ../lib/algs4.jar:. com.github.huffmancodec.BinaryDump 20 < ../file/CodeFile.bin" });
						outputToTextArea("编码结果: \n", p, outputTextArea);
						if (outputTextArea.getText().equals("")) {
							throw new IOException();
						}
					} catch (IOException e) {
						outputTextArea.setText("");
						JOptionPane.showConfirmDialog(frame, "编码失败", "错误", JOptionPane.PLAIN_MESSAGE);
					}
				}
			}
		});
		encodeButton.setBounds(310, 42, 130, 40);
		encodeButton.setBackground(Color.WHITE);
		encodeButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
		frame.getContentPane().add(encodeButton);

		JButton decodeButton = new JButton("<- 解码 <-");
		decodeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				firstClick = false;
				if (outputTextArea.getText().equals("编码结果: \n长度: 0 比特")) {
					// outputTextArea 为空
					inputTextArea.setText("");
				} else {
					// outputTextArea 非空
					inputTextArea.setText("");
					try {
						Process p = Runtime.getRuntime().exec(new String[] { shell[0], shell[1], "cd bin "
								+ "&& java -cp ../lib/algs4.jar:. com.github.huffmancodec.Huffman + < ../file/CodeFile.bin" });
						outputToTextArea("", p, inputTextArea);
						if (inputTextArea.getText().equals("")) {
							throw new IOException();
						}
					} catch (IOException e) {
						inputTextArea.setText("");
						JOptionPane.showConfirmDialog(frame, "解码失败", "错误", JOptionPane.PLAIN_MESSAGE);
					}
				}
			}
		});
		decodeButton.setBounds(310, 124, 130, 40);
		decodeButton.setBackground(Color.WHITE);
		decodeButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
		frame.getContentPane().add(decodeButton);

		JButton saveButton = new JButton("保存编码结果");
		saveButton.setToolTipText("将上一次成功编码的结果保存到二进制文件");
		saveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!new File("file/CodeFile.bin").isFile()) {
					JOptionPane.showConfirmDialog(frame, "请先进行编码", "提示", JOptionPane.PLAIN_MESSAGE);
				} else {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new File("."));
					chooser.setSelectedFile(new File("CodeFile.bin"));
					int result = chooser.showSaveDialog(frame);
					if (result == JFileChooser.APPROVE_OPTION) {
						try {
							Files.copy(Paths.get("file/CodeFile.bin"), chooser.getSelectedFile().toPath(),
									StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							JOptionPane.showConfirmDialog(frame, "保存时发生错误", "错误", JOptionPane.PLAIN_MESSAGE);
						}
					} else if (result == JFileChooser.ERROR_OPTION) {
						JOptionPane.showConfirmDialog(frame, "保存时发生错误", "错误", JOptionPane.PLAIN_MESSAGE);
					}
				}
			}
		});
		saveButton.setBounds(310, 206, 130, 40);
		saveButton.setBackground(Color.WHITE);
		saveButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
		frame.getContentPane().add(saveButton);

		JButton showButton = new JButton("显示霍夫曼树");
		showButton.setToolTipText("显示霍夫曼树");
		showButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (inputTextArea.getText().equals("")) {
					// inputTextArea 为空
					JOptionPane.showConfirmDialog(frame, "请先输入编码内容", "提示", JOptionPane.PLAIN_MESSAGE);
				} else {
					// inputTextArea 非空
					String text = inputTextArea.getText();
					try {
						Process p = Runtime.getRuntime().exec(new String[] { shell[0], shell[1], "cd bin "
								+ "&& echo \"" + text + "\" > ../file/TextFile.txt "
								+ "&& java -cp ../lib/algs4.jar:. com.github.huffmancodec.Huffman = < ../file/TextFile.txt > ../file/Tree.bin "
								+ "&& java -cp ../lib/algs4.jar:. com.github.huffmancodec.BinaryDump 20 < ../file/Tree.bin" });
						// TODO new Window
						outputToTextArea("霍夫曼树: \n", p, outputTextArea);
					} catch (IOException e) {
						JOptionPane.showConfirmDialog(frame, "编码失败", "错误", JOptionPane.PLAIN_MESSAGE);
					}
				}
			}
		});
		showButton.setBounds(310, 288, 130, 40);
		showButton.setBackground(Color.WHITE);
		showButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
		frame.getContentPane().add(showButton);
	}

	public static void outputToTextArea(String start, Process p, JTextArea ta) throws IOException {
		StringBuilder out = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;
		String previous = null;
		ta.setText(start);
		while ((line = br.readLine()) != null)
			if (!line.equals(previous)) {
				previous = line;
				out.append(line).append('\n');
				ta.append(line + "\n");
			}
		if (!ta.getText().equals("")) {
			// 消除行尾换行符
			ta.setText(ta.getText().substring(0, ta.getText().length() - 1));
		}
	}
}

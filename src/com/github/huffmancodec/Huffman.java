package com.github.huffmancodec;

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;
import edu.princeton.cs.algs4.MinPQ;

public class Huffman {

	// 扩展 ASCII 的字符数量
	private static final int R = 256;

	private Huffman() {
	}

	// 结点定义
	protected static class Node implements Comparable<Node> {
		final char ch;
		final int freq;
		final Node left, right;

		Node(char ch, int freq, Node left, Node right) {
			this.ch = ch;
			this.freq = freq;
			this.left = left;
			this.right = right;
		}

		// 是否为叶子结点
		private boolean isLeaf() {
			return (left == null) && (right == null);
		}

		public int compareTo(Node that) {
			return this.freq - that.freq;
		}
	}

	// 编码并输出
	public static void compress() {
		// 读取输入
		String s = BinaryStdIn.readString();
		char[] input = s.toCharArray();

		// 统计频率
		int[] freq = new int[R];
		for (int i = 0; i < input.length; i++)
			freq[input[i]]++;

		// 建立霍夫曼树
		Node root = buildTrie(freq);

		// 建立编译码表
		String[] st = new String[R];
		buildCode(st, root, "");

		// 输出霍夫曼树
		writeTrie(root);

		// 输出原始信息长度
		BinaryStdOut.write(input.length);

		// 对输入进行编码
		for (int i = 0; i < input.length; i++) {
			String code = st[input[i]];
			for (int j = 0; j < code.length(); j++) {
				if (code.charAt(j) == '0') {
					BinaryStdOut.write(false);
				} else if (code.charAt(j) == '1') {
					BinaryStdOut.write(true);
				} else
					throw new IllegalStateException("Illegal state");
			}
		}

		// 关闭输出流
		BinaryStdOut.close();
	}

	// 解码并输出
	public static void expand() {
		// 读取霍夫曼树
		Node root = readTrie();

		// 读取信息长度
		int length = BinaryStdIn.readInt();

		// 使用霍夫曼树解码
		for (int i = 0; i < length; i++) {
			Node x = root;
			while (!x.isLeaf()) {
				boolean bit = BinaryStdIn.readBoolean();
				if (bit)
					x = x.right;
				else
					x = x.left;
			}
			BinaryStdOut.write(x.ch, 8);
		}
		BinaryStdOut.close();
	}

	// 仅输出霍夫曼树
	public static void outputTrie() {
		// 读取输入
		String s = BinaryStdIn.readString();
		char[] input = s.toCharArray();

		// 统计频率
		int[] freq = new int[R];
		for (int i = 0; i < input.length; i++)
			freq[input[i]]++;

		// 建立霍夫曼树
		Node root = buildTrie(freq);

		// 建立编译码表
		String[] st = new String[R];
		buildCode(st, root, "");

		// 输出霍夫曼树
		BTreePrinter.printNode(root);
	}

	// 根据统计频率建立霍夫曼树
	public static Node buildTrie(int[] freq) {
		// 初始化优先队列
		MinPQ<Node> pq = new MinPQ<Node>();
		for (char i = 0; i < R; i++)
			if (freq[i] > 0)
				pq.insert(new Node(i, freq[i], null, null));

		// 只有一个字符
		if (pq.size() == 1) {
			if (freq['\0'] == 0)
				pq.insert(new Node('\0', 0, null, null));
			else
				pq.insert(new Node('\1', 0, null, null));
		}

		// 合并两棵树
		while (pq.size() > 1) {
			Node left = pq.delMin();
			Node right = pq.delMin();
			Node parent = new Node('\0', left.freq + right.freq, left, right);
			pq.insert(parent);
		}
		return pq.delMin();
	}

	// 建立编译码表
	private static void buildCode(String[] st, Node x, String s) {
		if (!x.isLeaf()) {
			buildCode(st, x.left, s + '0');
			buildCode(st, x.right, s + '1');
		} else {
			st[x.ch] = s;
		}
	}

	public static void writeTrie(Node x) {
		if (x.isLeaf()) {
			BinaryStdOut.write(true);
			BinaryStdOut.write(x.ch, 8);
			return;
		}
		BinaryStdOut.write(false);
		writeTrie(x.left);
		writeTrie(x.right);
	}

	private static Node readTrie() {
		boolean isLeaf = BinaryStdIn.readBoolean();
		if (isLeaf) {
			return new Node(BinaryStdIn.readChar(), -1, null, null);
		} else {
			return new Node('\0', -1, readTrie(), readTrie());
		}
	}

	public static void main(String[] args) {
		if (args[0].equals("-"))
			compress();
		else if (args[0].equals("+"))
			expand();
		else if (args[0].equals("="))
			outputTrie();
		else
			throw new IllegalArgumentException("命令行参数非法");
	}
}
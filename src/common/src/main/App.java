package common.src.main;

import org.jspace.*;

import java.util.Scanner;

public class App {
	public static void main(String[] args) {
		new Thread(new A()).start();

	}
}

class A implements Runnable {


	public A() {

	}

	public void run() {

	}
}

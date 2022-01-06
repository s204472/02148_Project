package common.src.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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

/*
 * This source file was generated by the Gradle 'init' task
 */
package bytejam.project;

import bytejam.project.turbo.Window;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        System.out.println("this is a test");

        Window window = Window.get();
        window.run();

    }
}

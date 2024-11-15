import chess.*;
import ui.Repl;

public class ClientMain {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        var repl = new Repl(serverUrl);
        repl.run();
    }
}
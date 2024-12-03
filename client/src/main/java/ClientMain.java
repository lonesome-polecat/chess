import chess.*;
import ui.Repl;

public class ClientMain {
    public static void main(String[] args) {
        var serverUrl = "localhost:";
        var repl = new Repl(serverUrl);
        repl.run();
    }
}
package redis.command;

import java.util.List;

public class CommandRequest {

    private final String command;
    private final List<String> arguments;

    public CommandRequest(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public String command() {
        return command;
    }

    public List<String> arguments() {
        return arguments;
    }
}
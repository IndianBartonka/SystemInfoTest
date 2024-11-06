package pl.indianbartonka.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public final class ArgumentParser {

    private final List<Arg> argsList = new LinkedList<>();

    public ArgumentParser(final String[] args) {
        final List<Arg> parsedArgs = new ArrayList<>();

        for (final String arg : args) {
            if (arg.startsWith("-")) {
                final String[] splitArg = arg.substring(1).split(":", 2);
                if (splitArg.length == 2) {
                    parsedArgs.add(new Arg(splitArg[0], splitArg[1]));
                } else {
                    parsedArgs.add(new Arg(splitArg[0], null));
                }
            }
        }

        this.argsList.addAll(parsedArgs);
    }

    @Nullable
    public Arg getArgByName(final String name) {
        return this.argsList.stream()
                .filter(arg -> arg.name().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    public boolean contains(final String name) {
        return this.argsList.stream().anyMatch(arg -> arg.name().equalsIgnoreCase(name));
    }

    public boolean isAnyArgument(){
        return !this.argsList.isEmpty();
    }

    public List<Arg> getArgsList() {
        return this.argsList;
    }
}

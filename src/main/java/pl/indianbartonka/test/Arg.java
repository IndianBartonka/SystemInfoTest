package pl.indianbartonka.test;

import org.jetbrains.annotations.Nullable;

public record Arg(String name, @Nullable String value) {
}
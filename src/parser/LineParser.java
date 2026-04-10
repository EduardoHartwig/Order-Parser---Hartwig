package parser;

public sealed interface LineParser<T> permits OrderLineParser {

    T parse(String line);
}

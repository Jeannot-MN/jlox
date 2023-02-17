package com.jmn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jmn.TokenType.AND;
import static com.jmn.TokenType.BANG;
import static com.jmn.TokenType.BANG_EQUAL;
import static com.jmn.TokenType.CLASS;
import static com.jmn.TokenType.COMMA;
import static com.jmn.TokenType.DOT;
import static com.jmn.TokenType.ELSE;
import static com.jmn.TokenType.EOF;
import static com.jmn.TokenType.EQUAL;
import static com.jmn.TokenType.EQUAL_EQUAL;
import static com.jmn.TokenType.FALSE;
import static com.jmn.TokenType.FOR;
import static com.jmn.TokenType.FUN;
import static com.jmn.TokenType.GREATER;
import static com.jmn.TokenType.GREATER_EQUAL;
import static com.jmn.TokenType.IDENTIFIER;
import static com.jmn.TokenType.IF;
import static com.jmn.TokenType.LEFT_BRACE;
import static com.jmn.TokenType.LEFT_PAREN;
import static com.jmn.TokenType.LESS;
import static com.jmn.TokenType.LESS_EQUAL;
import static com.jmn.TokenType.MINUS;
import static com.jmn.TokenType.NIL;
import static com.jmn.TokenType.NUMBER;
import static com.jmn.TokenType.OR;
import static com.jmn.TokenType.PLUS;
import static com.jmn.TokenType.PRINT;
import static com.jmn.TokenType.RETURN;
import static com.jmn.TokenType.RIGHT_BRACE;
import static com.jmn.TokenType.RIGHT_PAREN;
import static com.jmn.TokenType.SEMICOLON;
import static com.jmn.TokenType.SLASH;
import static com.jmn.TokenType.STAR;
import static com.jmn.TokenType.STRING;
import static com.jmn.TokenType.SUPER;
import static com.jmn.TokenType.THIS;
import static com.jmn.TokenType.TRUE;
import static com.jmn.TokenType.VAR;
import static com.jmn.TokenType.WHILE;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> execute() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = getNextChar();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) getNextChar();
                } else {
                    addToken(SLASH);
                }
                break;
            case '"':
                string();
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Main.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            getNextChar();
        }
        if (isAtEnd()) {
            Main.error(line, "Unterminated string.");
            return;
        }

        getNextChar();
        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) getNextChar();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            getNextChar();
            while (isDigit(peek())) getNextChar();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) getNextChar();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }

    private char getNextChar() {
        return source.charAt(current++);
    }

    private boolean match(char c) {
        if (isAtEnd() || source.charAt(current) != c) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || ((c >= 'A' && c <= 'Z')) || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}

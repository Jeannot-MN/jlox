package com.jmn;

import java.util.ArrayList;
import java.util.List;

import static com.jmn.TokenType.BANG;
import static com.jmn.TokenType.BANG_EQUAL;
import static com.jmn.TokenType.COMMA;
import static com.jmn.TokenType.DOT;
import static com.jmn.TokenType.EOF;
import static com.jmn.TokenType.EQUAL;
import static com.jmn.TokenType.EQUAL_EQUAL;
import static com.jmn.TokenType.GREATER;
import static com.jmn.TokenType.GREATER_EQUAL;
import static com.jmn.TokenType.LEFT_BRACE;
import static com.jmn.TokenType.LEFT_PAREN;
import static com.jmn.TokenType.LESS;
import static com.jmn.TokenType.LESS_EQUAL;
import static com.jmn.TokenType.MINUS;
import static com.jmn.TokenType.PLUS;
import static com.jmn.TokenType.RIGHT_BRACE;
import static com.jmn.TokenType.RIGHT_PAREN;
import static com.jmn.TokenType.SEMICOLON;
import static com.jmn.TokenType.SLASH;
import static com.jmn.TokenType.STAR;
import static com.jmn.TokenType.STRING;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

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
            case '"':
                while (peek()!='"' && !isAtEnd()){
                    if(peek()== '\n') line++;
                    getNextChar();
                }
                if (isAtEnd()){
                    Main.error(line, "Unterminated string.");
                }

                getNextChar();
                // Trim the surrounding quotes.
                String value = source.substring(start + 1, current - 1);
                addToken(STRING, value);
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
                Main.error(line, "Unexpected character.");
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

    private char getNextChar() {
        return source.charAt(current++);
    }

    private boolean match(char c) {
        if (isAtEnd() || source.charAt(current) == c) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}

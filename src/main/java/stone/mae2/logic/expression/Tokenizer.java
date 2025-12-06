package stone.mae2.logic.expression;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final String input;
    private int pos;

    public Tokenizer(String input) {
        this.input = input;
        this.pos = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char current = input.charAt(pos);

            if (Character.isWhitespace(current)) {
                pos++;
                continue;
            }

            if (Character.isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }

            if (isIdentifierStart(current)) {
                tokens.add(readIdentifier());
                continue;
            }

            switch (current) {
                case '>':
                    if (match('=')) {
                        tokens.add(new Token(TokenType.GTE, ">=", pos - 2));
                    } else {
                        tokens.add(new Token(TokenType.GT, ">", pos));
                        pos++;
                    }
                    break;
                case '<':
                    if (match('=')) {
                        tokens.add(new Token(TokenType.LTE, "<=", pos - 2));
                    } else {
                        tokens.add(new Token(TokenType.LT, "<", pos));
                        pos++;
                    }
                    break;
                case '=':
                    if (match('=')) {
                        tokens.add(new Token(TokenType.EQ, "==", pos - 2));
                    } else {
                        throw new RuntimeException(
                                "Unexpected character '=' at position " + pos + ". Did you mean '=='?");
                    }
                    break;
                case '!':
                    if (match('=')) {
                        tokens.add(new Token(TokenType.NEQ, "!=", pos - 2));
                    } else {
                        tokens.add(new Token(TokenType.NOT, "!", pos));
                        pos++;
                    }
                    break;
                case '^':
                    tokens.add(new Token(TokenType.XOR, "^", pos));
                    pos++;
                    break;
                case '&':
                    if (match('&')) {
                        tokens.add(new Token(TokenType.AND, "&&", pos - 2));
                    } else {
                        throw new RuntimeException(
                                "Unexpected character '&' at position " + pos + ". Did you mean '&&'?");
                    }
                    break;
                case '|':
                    if (match('|')) {
                        tokens.add(new Token(TokenType.OR, "||", pos - 2));
                    } else {
                        throw new RuntimeException(
                                "Unexpected character '|' at position " + pos + ". Did you mean '||'?");
                    }
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPAREN, "(", pos));
                    pos++;
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPAREN, ")", pos));
                    pos++;
                    break;
                default:
                    throw new RuntimeException("Unexpected character '" + current + "' at position " + pos);
            }
        }
        tokens.add(new Token(TokenType.EOF, "", pos));
        return tokens;
    }

    private boolean match(char expected) {
        if (pos + 1 < input.length() && input.charAt(pos + 1) == expected) {
            pos += 2;
            return true;
        }
        return false;
    }

    private Token readNumber() {
        int start = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }
        return new Token(TokenType.NUMBER, input.substring(start, pos), start);
    }

    private Token readIdentifier() {
        int start = pos;
        while (pos < input.length() && isIdentifierPart(input.charAt(pos))) {
            pos++;
        }
        return new Token(TokenType.IDENTIFIER, input.substring(start, pos), start);
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '*';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == ':' || c == '*' || c == '/' || c == '.' || c == '-';
    }
}

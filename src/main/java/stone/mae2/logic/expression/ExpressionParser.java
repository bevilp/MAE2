package stone.mae2.logic.expression;

import java.util.List;

public class ExpressionParser {
    private final List<Token> tokens;
    private int pos;

    public ExpressionParser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public Node parse() {
        Node node = parseExpression();
        if (!match(TokenType.EOF)) {
            throw new RuntimeException("Unexpected token at end of expression: " + peek());
        }
        return node;
    }

    // Expression -> OrTerm
    private Node parseExpression() {
        return parseOrTerm();
    }

    // OrTerm -> XorTerm ( '||' XorTerm )*
    private Node parseOrTerm() {
        Node left = parseXorTerm();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Node right = parseXorTerm();
            left = new BinaryNode(left, operator.getType(), right);
        }
        return left;
    }

    // XorTerm -> AndTerm ( '^' AndTerm )*
    private Node parseXorTerm() {
        Node left = parseAndTerm();
        while (match(TokenType.XOR)) {
            Token operator = previous();
            Node right = parseAndTerm();
            left = new BinaryNode(left, operator.getType(), right);
        }
        return left;
    }

    // AndTerm -> EqualityTerm ( '&&' EqualityTerm )*
    private Node parseAndTerm() {
        Node left = parseEqualityTerm();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Node right = parseEqualityTerm();
            left = new BinaryNode(left, operator.getType(), right);
        }
        return left;
    }

    // EqualityTerm -> ComparisonTerm ( ('==' | '!=') ComparisonTerm )*
    private Node parseEqualityTerm() {
        Node left = parseComparisonTerm();
        while (match(TokenType.EQ, TokenType.NEQ)) {
            Token operator = previous();
            Node right = parseComparisonTerm();
            left = new BinaryNode(left, operator.getType(), right);
        }
        return left;
    }

    // ComparisonTerm -> UnaryTerm ( ('>' | '<' | '>=' | '<=') UnaryTerm )*
    private Node parseComparisonTerm() {
        Node left = parseUnaryTerm();
        while (match(TokenType.GT, TokenType.LT, TokenType.GTE, TokenType.LTE)) {
            Token operator = previous();
            Node right = parseUnaryTerm();
            left = new BinaryNode(left, operator.getType(), right);
        }
        return left;
    }

    // UnaryTerm -> '!' UnaryTerm | Primary
    private Node parseUnaryTerm() {
        if (match(TokenType.NOT)) {
            Token operator = previous();
            Node operand = parseUnaryTerm(); // Right-associative for multiple NOTs
            return new UnaryNode(operator.getType(), operand);
        }
        return parsePrimary();
    }

    // Primary -> '(' Expression ')' | Identifier | Number
    private Node parsePrimary() {
        if (match(TokenType.LPAREN)) {
            Node expression = parseExpression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return expression;
        }

        if (match(TokenType.NUMBER)) {
            return new LiteralNode(Long.parseLong(previous().getValue()));
        }

        if (match(TokenType.IDENTIFIER)) {
            return new TagNode(previous().getValue());
        }

        throw new RuntimeException("Expect expression. Found: " + peek());
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return type == TokenType.EOF;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd())
            pos++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw new RuntimeException(message);
    }
}

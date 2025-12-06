package stone.mae2.logic.expression;

/**
 * Represents a unary operation node in the expression AST.
 * Currently supports the NOT (!) operator.
 */
public class UnaryNode extends Node {
    private final TokenType operator;
    private final Node operand;

    public UnaryNode(TokenType operator, Node operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public TokenType getOperator() {
        return operator;
    }

    public Node getOperand() {
        return operand;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "UnaryNode{operator=" + operator + ", operand=" + operand + "}";
    }
}

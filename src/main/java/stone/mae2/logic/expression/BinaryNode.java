package stone.mae2.logic.expression;

public class BinaryNode extends Node {
    private final Node left;
    private final Node right;
    private final TokenType operator;

    public BinaryNode(Node left, TokenType operator, Node right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public TokenType getOperator() {
        return operator;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}

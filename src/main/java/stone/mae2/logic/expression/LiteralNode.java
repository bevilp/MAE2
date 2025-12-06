package stone.mae2.logic.expression;

public class LiteralNode extends Node {
    private final long value;

    public LiteralNode(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}

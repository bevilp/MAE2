package stone.mae2.logic.expression;

public abstract class Node {
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visit(BinaryNode node);

        R visit(UnaryNode node);

        R visit(LiteralNode node);

        R visit(TagNode node);
    }
}

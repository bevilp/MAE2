package stone.mae2.logic.expression;

public class TagNode extends Node {
    private final String tag;

    public TagNode(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}

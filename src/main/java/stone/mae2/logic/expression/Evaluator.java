package stone.mae2.logic.expression;

public class Evaluator implements Node.Visitor<Object> {
    private final InventoryContext context;

    public Evaluator(InventoryContext context) {
        this.context = context;
    }

    public boolean evaluate(Node node) {
        Object result = node.accept(this);
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        throw new RuntimeException("Expression did not evaluate to a boolean.");
    }

    @Override
    public Object visit(BinaryNode node) {
        Object left = node.getLeft().accept(this);
        Object right = node.getRight().accept(this);

        switch (node.getOperator()) {
            case OR:
                return asBoolean(left) || asBoolean(right);
            case AND:
                return asBoolean(left) && asBoolean(right);
            case XOR:
                return asBoolean(left) ^ asBoolean(right);
            case EQ:
                return left.equals(right);
            case NEQ:
                return !left.equals(right);
            case GT:
                return asLong(left) > asLong(right);
            case LT:
                return asLong(left) < asLong(right);
            case GTE:
                return asLong(left) >= asLong(right);
            case LTE:
                return asLong(left) <= asLong(right);
            default:
                throw new RuntimeException("Unknown operator: " + node.getOperator());
        }
    }

    @Override
    public Object visit(UnaryNode node) {
        Object operand = node.getOperand().accept(this);

        switch (node.getOperator()) {
            case NOT:
                return !asBoolean(operand);
            default:
                throw new RuntimeException("Unknown unary operator: " + node.getOperator());
        }
    }

    @Override
    public Object visit(LiteralNode node) {
        return node.getValue();
    }

    @Override
    public Object visit(TagNode node) {
        return context.getCount(node.getTag());
    }

    private boolean asBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        throw new RuntimeException("Expected boolean, got " + obj.getClass().getSimpleName());
    }

    private long asLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        throw new RuntimeException("Expected number, got " + obj.getClass().getSimpleName());
    }
}

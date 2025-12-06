package stone.mae2.logic.expression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the expression parser.
 */
class ExpressionParserTest {

    @Test
    void testSimpleComparison() {
        Node node = parse("minecraft:stone > 10");
        
        assertNotNull(node);
        assertTrue(node instanceof BinaryNode);
        BinaryNode binary = (BinaryNode) node;
        assertEquals(TokenType.GT, binary.getOperator());
    }

    @Test
    void testLogicalAnd() {
        Node node = parse("minecraft:stone > 10 && minecraft:dirt < 5");
        
        assertNotNull(node);
        assertTrue(node instanceof BinaryNode);
        BinaryNode binary = (BinaryNode) node;
        assertEquals(TokenType.AND, binary.getOperator());
    }

    @Test
    void testLogicalOr() {
        Node node = parse("minecraft:stone > 10 || minecraft:dirt < 5");
        
        assertNotNull(node);
        assertTrue(node instanceof BinaryNode);
        BinaryNode binary = (BinaryNode) node;
        assertEquals(TokenType.OR, binary.getOperator());
    }

    @Test
    void testParentheses() {
        Node node = parse("(minecraft:stone > 10)");
        
        assertNotNull(node);
        assertTrue(node instanceof BinaryNode);
    }

    @Test
    void testOperatorPrecedence() {
        // && has higher precedence than ||
        // Should parse as: (a && b) || c
        Node node = parse("a > 1 || b > 2 && c > 3");
        
        assertTrue(node instanceof BinaryNode);
        BinaryNode root = (BinaryNode) node;
        assertEquals(TokenType.OR, root.getOperator());
        
        // Right side should be AND
        assertTrue(root.getRight() instanceof BinaryNode);
        BinaryNode rightAnd = (BinaryNode) root.getRight();
        assertEquals(TokenType.AND, rightAnd.getOperator());
    }

    @Test
    void testComparisonOperatorPrecedence() {
        // All comparison operators have same precedence
        Node node = parse("a > 1 == b < 2");
        
        assertTrue(node instanceof BinaryNode);
        BinaryNode root = (BinaryNode) node;
        assertEquals(TokenType.EQ, root.getOperator());
    }

    @Test
    void testNestedParentheses() {
        Node node = parse("((minecraft:stone > 10))");
        
        assertNotNull(node);
        assertTrue(node instanceof BinaryNode);
    }

    @Test
    void testComplexExpression() {
        Node node = parse("(minecraft:iron >= 100 && minecraft:gold >= 50) || minecraft:diamond > 10");
        
        assertNotNull(node);
        assertTrue(node instanceof BinaryNode);
        BinaryNode root = (BinaryNode) node;
        assertEquals(TokenType.OR, root.getOperator());
    }

    @ParameterizedTest
    @CsvSource({
        "minecraft:stone > 10, GT",
        "minecraft:stone < 10, LT",
        "minecraft:stone >= 10, GTE",
        "minecraft:stone <= 10, LTE",
        "minecraft:stone == 10, EQ",
        "minecraft:stone != 10, NEQ"
    })
    void testAllComparisonOperators(String expression, String expectedOp) {
        Node node = parse(expression);
        
        assertTrue(node instanceof BinaryNode);
        BinaryNode binary = (BinaryNode) node;
        assertEquals(TokenType.valueOf(expectedOp), binary.getOperator());
    }

    @Test
    void testTagNode() {
        Node node = parse("minecraft:stone > 10");
        
        BinaryNode binary = (BinaryNode) node;
        assertTrue(binary.getLeft() instanceof TagNode);
        TagNode tag = (TagNode) binary.getLeft();
        assertEquals("minecraft:stone", tag.getTag());
    }

    @Test
    void testLiteralNode() {
        Node node = parse("minecraft:stone > 10");
        
        BinaryNode binary = (BinaryNode) node;
        assertTrue(binary.getRight() instanceof LiteralNode);
        LiteralNode literal = (LiteralNode) binary.getRight();
        assertEquals(10L, literal.getValue());
    }

    @Test
    void testWildcardTag() {
        Node node = parse("minecraft:*_log > 5");
        
        BinaryNode binary = (BinaryNode) node;
        assertTrue(binary.getLeft() instanceof TagNode);
        TagNode tag = (TagNode) binary.getLeft();
        assertEquals("minecraft:*_log", tag.getTag());
    }

    @Test
    void testUnexpectedToken() {
        assertThrows(RuntimeException.class, () -> {
            parse("minecraft:stone >");
        });
    }

    @Test
    void testMissingClosingParenthesis() {
        assertThrows(RuntimeException.class, () -> {
            parse("(minecraft:stone > 10");
        });
    }

    @Test
    void testExtraClosingParenthesis() {
        assertThrows(RuntimeException.class, () -> {
            parse("minecraft:stone > 10)");
        });
    }

    @Test
    void testEmptyExpression() {
        assertThrows(RuntimeException.class, () -> {
            parse("");
        });
    }

    @Test
    void testInvalidExpression() {
        assertThrows(RuntimeException.class, () -> {
            parse("&& ||");
        });
    }

    @Test
    void testNotOperator() {
        Node node = parse("!(minecraft:diamond > 10)");
        
        assertNotNull(node);
        assertTrue(node instanceof UnaryNode);
        UnaryNode unary = (UnaryNode) node;
        assertEquals(TokenType.NOT, unary.getOperator());
        assertTrue(unary.getOperand() instanceof BinaryNode);
    }

    @Test
    void testDoubleNot() {
        Node node = parse("!!(minecraft:diamond > 10)");
        
        assertNotNull(node);
        assertTrue(node instanceof UnaryNode);
        UnaryNode outer = (UnaryNode) node;
        assertEquals(TokenType.NOT, outer.getOperator());
        assertTrue(outer.getOperand() instanceof UnaryNode);
        UnaryNode inner = (UnaryNode) outer.getOperand();
        assertEquals(TokenType.NOT, inner.getOperator());
    }

    @Test
    void testNotWithComplexExpression() {
        Node node = parse("!(minecraft:iron > 10 && minecraft:gold > 5)");
        
        assertNotNull(node);
        assertTrue(node instanceof UnaryNode);
        UnaryNode unary = (UnaryNode) node;
        assertEquals(TokenType.NOT, unary.getOperator());
        assertTrue(unary.getOperand() instanceof BinaryNode);
        BinaryNode binary = (BinaryNode) unary.getOperand();
        assertEquals(TokenType.AND, binary.getOperator());
    }

    @Test
    void testXorOperator() {
        Node node = parse("minecraft:iron > 10 ^ minecraft:gold > 5");
        
        assertNotNull(node);
        assertTrue(node instanceof BinaryNode);
        BinaryNode binary = (BinaryNode) node;
        assertEquals(TokenType.XOR, binary.getOperator());
    }

    @Test
    void testXorPrecedence() {
        // XOR should have same precedence as OR, but lower than AND
        // a || b ^ c && d should parse as: (a || (b ^ (c && d)))
        Node node = parse("a > 1 || b > 2 ^ c > 3 && d > 4");
        
        assertTrue(node instanceof BinaryNode);
        BinaryNode root = (BinaryNode) node;
        assertEquals(TokenType.OR, root.getOperator());
        
        // Right side should be XOR
        assertTrue(root.getRight() instanceof BinaryNode);
        BinaryNode xor = (BinaryNode) root.getRight();
        assertEquals(TokenType.XOR, xor.getOperator());
        
        // XOR right side should be AND
        assertTrue(xor.getRight() instanceof BinaryNode);
        BinaryNode and = (BinaryNode) xor.getRight();
        assertEquals(TokenType.AND, and.getOperator());
    }

    @Test
    void testNotWithXor() {
        Node node = parse("!(a > 1 ^ b > 2)");
        
        assertTrue(node instanceof UnaryNode);
        UnaryNode unary = (UnaryNode) node;
        assertEquals(TokenType.NOT, unary.getOperator());
        assertTrue(unary.getOperand() instanceof BinaryNode);
        BinaryNode xor = (BinaryNode) unary.getOperand();
        assertEquals(TokenType.XOR, xor.getOperator());
    }

    @Test
    void testComplexNotExpression() {
        Node node = parse("!(a > 1) && !(b < 2)");
        
        assertTrue(node instanceof BinaryNode);
        BinaryNode and = (BinaryNode) node;
        assertEquals(TokenType.AND, and.getOperator());
        assertTrue(and.getLeft() instanceof UnaryNode);
        assertTrue(and.getRight() instanceof UnaryNode);
    }

    private Node parse(String expression) {
        Tokenizer tokenizer = new Tokenizer(expression);
        List<Token> tokens = tokenizer.tokenize();
        ExpressionParser parser = new ExpressionParser(tokens);
        return parser.parse();
    }
}

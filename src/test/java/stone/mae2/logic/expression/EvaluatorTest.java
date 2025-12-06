package stone.mae2.logic.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the expression evaluator.
 */
class EvaluatorTest {

    private MockInventoryContext inventory;

    @BeforeEach
    void setUp() {
        // Set up test inventory explicitly
        inventory = new MockInventoryContext();
        inventory.setCount("minecraft:stone", 64L);
        inventory.setCount("minecraft:oak_log", 10L);
        inventory.setCount("minecraft:birch_log", 5L);
        inventory.setCount("minecraft:dirt", 0L);
        inventory.setCount("minecraft:diamond", 32L);
    }

    @Test
    void testSimpleGreaterThan() {
        assertTrue(evaluate("minecraft:stone > 10")); // stone = 64
        assertFalse(evaluate("minecraft:stone > 100")); // stone = 64
    }

    @Test
    void testSimpleLessThan() {
        assertTrue(evaluate("minecraft:dirt < 10")); // dirt = 0
        assertFalse(evaluate("minecraft:stone < 10")); // stone = 64
    }

    @Test
    void testGreaterThanOrEqual() {
        assertTrue(evaluate("minecraft:stone >= 64")); // stone = 64
        assertTrue(evaluate("minecraft:stone >= 10")); // stone = 64
        assertFalse(evaluate("minecraft:stone >= 100")); // stone = 64
    }

    @Test
    void testLessThanOrEqual() {
        assertTrue(evaluate("minecraft:dirt <= 0")); // dirt = 0
        assertTrue(evaluate("minecraft:dirt <= 10")); // dirt = 0
        assertFalse(evaluate("minecraft:stone <= 10")); // stone = 64
    }

    @Test
    void testEquals() {
        assertTrue(evaluate("minecraft:stone == 64")); // stone = 64
        assertFalse(evaluate("minecraft:stone == 10")); // stone = 64
    }

    @Test
    void testNotEquals() {
        assertTrue(evaluate("minecraft:stone != 10")); // stone = 64
        assertFalse(evaluate("minecraft:stone != 64")); // stone = 64
    }

    @Test
    void testLogicalAnd() {
        assertTrue(evaluate("minecraft:stone > 10 && minecraft:diamond > 10")); // both > 10
        assertFalse(evaluate("minecraft:stone > 10 && minecraft:dirt > 10")); // dirt = 0
        assertFalse(evaluate("minecraft:dirt > 10 && minecraft:stone > 10")); // dirt = 0
    }

    @Test
    void testLogicalOr() {
        assertTrue(evaluate("minecraft:stone > 10 || minecraft:dirt > 10")); // stone > 10
        assertTrue(evaluate("minecraft:dirt > 10 || minecraft:stone > 10")); // stone > 10
        assertFalse(evaluate("minecraft:dirt > 10 || minecraft:dirt > 5")); // dirt = 0
    }

    @Test
    void testComplexExpression() {
        // (stone > 10 && diamond > 10) || dirt > 10
        assertTrue(evaluate("(minecraft:stone > 10 && minecraft:diamond > 10) || minecraft:dirt > 10"));
        
        // stone > 10 && (diamond > 10 || dirt > 10)
        assertTrue(evaluate("minecraft:stone > 10 && (minecraft:diamond > 10 || minecraft:dirt > 10)"));
    }

    @Test
    void testOperatorPrecedence() {
        // a || b && c should parse as a || (b && c)
        // stone > 100 || stone > 10 && diamond > 10
        // false || (true && true) = true
        assertTrue(evaluate("minecraft:stone > 100 || minecraft:stone > 10 && minecraft:diamond > 10"));
    }

    @Test
    void testWildcardPattern() {
        // *_log should match oak_log (10) + birch_log (5) = 15
        assertTrue(evaluate("minecraft:*_log > 10"));
        assertTrue(evaluate("minecraft:*_log == 15"));
        assertFalse(evaluate("minecraft:*_log > 20"));
    }

    @Test
    void testWildcardPatternComplex() {
        // Test wildcard with prefix
        inventory.setCount("minecraft:oak_planks", 20L);
        inventory.setCount("minecraft:birch_planks", 10L);
        
        assertTrue(evaluate("minecraft:*_planks >= 30")); // 20 + 10 = 30
    }

    @Test
    void testNonExistentItem() {
        assertFalse(evaluate("minecraft:nonexistent > 0"));
        assertTrue(evaluate("minecraft:nonexistent == 0"));
    }

    @Test
    void testZeroComparison() {
        assertTrue(evaluate("minecraft:dirt == 0")); // dirt = 0
        assertTrue(evaluate("minecraft:dirt <= 0")); // dirt = 0
        assertFalse(evaluate("minecraft:dirt > 0")); // dirt = 0
    }

    @Test
    void testShortCircuitAnd() {
        // If first condition is false, second shouldn't be evaluated
        assertFalse(evaluate("minecraft:dirt > 10 && minecraft:stone > 10"));
    }

    @Test
    void testShortCircuitOr() {
        // If first condition is true, second shouldn't be evaluated
        assertTrue(evaluate("minecraft:stone > 10 || minecraft:dirt > 10"));
    }

    @Test
    void testNestedParentheses() {
        assertTrue(evaluate("((minecraft:stone > 10))"));
        assertTrue(evaluate("(((minecraft:stone > 10 && minecraft:diamond > 10)))"));
    }

    @Test
    void testMultipleConditions() {
        assertTrue(evaluate("minecraft:stone > 10 && minecraft:diamond > 10 && minecraft:oak_log > 5"));
        assertFalse(evaluate("minecraft:stone > 10 && minecraft:diamond > 10 && minecraft:dirt > 5"));
    }

    @Test
    void testMixedOperators() {
        // stone > 10 && diamond > 10 || dirt > 10
        // (true && true) || false = true
        assertTrue(evaluate("minecraft:stone > 10 && minecraft:diamond > 10 || minecraft:dirt > 10"));
        
        // stone > 100 && diamond > 10 || dirt > 10
        // (false && true) || false = false
        assertFalse(evaluate("minecraft:stone > 100 && minecraft:diamond > 10 || minecraft:dirt > 10"));
    }

    @ParameterizedTest
    @CsvSource({
        "minecraft:stone > 10, true",
        "minecraft:stone > 100, false",
        "minecraft:stone == 64, true",
        "minecraft:diamond >= 32, true",
        "minecraft:diamond <= 32, true",
        "minecraft:dirt != 0, false",
        "minecraft:oak_log > 5, true"
    })
    void testVariousExpressions(String expression, boolean expected) {
        assertEquals(expected, evaluate(expression));
    }

    @Test
    void testCustomInventory() {
        MockInventoryContext customContext = new MockInventoryContext();
        customContext.clear();
        customContext.setCount("test:item1", 100L);
        customContext.setCount("test:item2", 50L);
        
        Evaluator customEvaluator = new Evaluator(customContext);
        
        assertTrue(customEvaluator.evaluate(parse("test:item1 > test:item2")));
        assertTrue(customEvaluator.evaluate(parse("test:item1 >= 100 && test:item2 >= 50")));
    }

    @Test
    void testNotOperator() {
        // NOT of true = false
        assertFalse(evaluate("!(minecraft:stone > 10)"));
        // NOT of false = true
        assertTrue(evaluate("!(minecraft:stone > 100)"));
    }

    @Test
    void testDoubleNot() {
        // !! should cancel out
        assertTrue(evaluate("!!(minecraft:stone > 10)"));
        assertFalse(evaluate("!!(minecraft:stone > 100)"));
    }

    @Test
    void testNotWithComplexExpression() {
        // !(true && true) = false
        assertFalse(evaluate("!(minecraft:stone > 10 && minecraft:diamond > 10)"));
        // !(true && false) = true
        assertTrue(evaluate("!(minecraft:stone > 10 && minecraft:diamond > 100)"));
        // !(false || false) = true
        assertTrue(evaluate("!(minecraft:stone > 100 || minecraft:diamond > 100)"));
    }

    @Test
    void testXorOperator() {
        // true ^ false = true
        assertTrue(evaluate("minecraft:stone > 10 ^ minecraft:stone > 100"));
        // false ^ true = true
        assertTrue(evaluate("minecraft:stone > 100 ^ minecraft:stone > 10"));
        // true ^ true = false
        assertFalse(evaluate("minecraft:stone > 10 ^ minecraft:diamond > 10"));
        // false ^ false = false
        assertFalse(evaluate("minecraft:stone > 100 ^ minecraft:diamond > 100"));
    }

    @Test
    void testXorWithAnd() {
        // (true && true) ^ false = true ^ false = true
        assertTrue(evaluate("(minecraft:stone > 10 && minecraft:diamond > 10) ^ minecraft:stone > 100"));
        // true ^ (true && false) = true ^ false = true
        assertTrue(evaluate("minecraft:stone > 10 ^ (minecraft:stone > 10 && minecraft:diamond > 100)"));
    }

    @Test
    void testNotWithXor() {
        // !(true ^ false) = !true = false
        assertFalse(evaluate("!(minecraft:stone > 10 ^ minecraft:stone > 100)"));
        // !(true ^ true) = !false = true
        assertTrue(evaluate("!(minecraft:stone > 10 ^ minecraft:diamond > 10)"));
    }

    @Test
    void testComplexNotAndXor() {
        // !(a) && !(b) where a=true, b=false -> false && true = false
        assertFalse(evaluate("!(minecraft:stone > 10) && !(minecraft:stone > 100)"));
        // !(a) ^ !(b) where a=true, b=false -> false ^ true = true
        assertTrue(evaluate("!(minecraft:stone > 10) ^ !(minecraft:stone > 100)"));
    }

    @Test
    void testNotZeroComparison() {
        // minecraft:dirt is 0, so !(dirt == 0) should be false
        assertFalse(evaluate("!(minecraft:dirt == 0)"));
        // !(dirt > 0) should be true
        assertTrue(evaluate("!(minecraft:dirt > 0)"));
    }

    @Test
    void testXorPrecedence() {
        // a || b ^ c && d
        // stone > 10 = true, stone > 100 = false, diamond > 10 = true, diamond > 100 = false
        // true || false ^ true && false
        // true || false ^ false
        // true || false
        // true
        assertTrue(evaluate("minecraft:stone > 10 || minecraft:stone > 100 ^ minecraft:diamond > 10 && minecraft:diamond > 100"));
    }

    private boolean evaluate(String expression) {
        Evaluator evaluator = new Evaluator(inventory);
        return evaluator.evaluate(parse(expression));
    }

    private Node parse(String expression) {
        Tokenizer tokenizer = new Tokenizer(expression);
        List<Token> tokens = tokenizer.tokenize();
        ExpressionParser parser = new ExpressionParser(tokens);
        return parser.parse();
    }
}

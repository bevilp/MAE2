package stone.mae2.logic.expression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the expression tokenizer.
 */
class TokenizerTest {

    @Test
    void testSimpleIdentifier() {
        Tokenizer tokenizer = new Tokenizer("minecraft:stone");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(2, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("minecraft:stone", tokens.get(0).getValue());
        assertEquals(TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    void testNumber() {
        Tokenizer tokenizer = new Tokenizer("123");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(2, tokens.size());
        assertEquals(TokenType.NUMBER, tokens.get(0).getType());
        assertEquals("123", tokens.get(0).getValue());
        assertEquals(TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    void testComparisonOperators() {
        Tokenizer tokenizer = new Tokenizer("> < >= <= == !=");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(7, tokens.size()); // 6 operators + EOF
        assertEquals(TokenType.GT, tokens.get(0).getType());
        assertEquals(TokenType.LT, tokens.get(1).getType());
        assertEquals(TokenType.GTE, tokens.get(2).getType());
        assertEquals(TokenType.LTE, tokens.get(3).getType());
        assertEquals(TokenType.EQ, tokens.get(4).getType());
        assertEquals(TokenType.NEQ, tokens.get(5).getType());
    }

    @Test
    void testLogicalOperators() {
        Tokenizer tokenizer = new Tokenizer("&& ||");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(3, tokens.size()); // 2 operators + EOF
        assertEquals(TokenType.AND, tokens.get(0).getType());
        assertEquals(TokenType.OR, tokens.get(1).getType());
    }

    @Test
    void testParentheses() {
        Tokenizer tokenizer = new Tokenizer("( )");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(3, tokens.size()); // 2 parens + EOF
        assertEquals(TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(TokenType.RPAREN, tokens.get(1).getType());
    }

    @Test
    void testComplexExpression() {
        Tokenizer tokenizer = new Tokenizer("minecraft:stone > 10 && minecraft:dirt < 5");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(8, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("minecraft:stone", tokens.get(0).getValue());
        assertEquals(TokenType.GT, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals("10", tokens.get(2).getValue());
        assertEquals(TokenType.AND, tokens.get(3).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(4).getType());
        assertEquals("minecraft:dirt", tokens.get(4).getValue());
        assertEquals(TokenType.LT, tokens.get(5).getType());
        assertEquals(TokenType.NUMBER, tokens.get(6).getType());
        assertEquals("5", tokens.get(6).getValue());
        assertEquals(TokenType.EOF, tokens.get(7).getType());
    }

    @Test
    void testWildcardPattern() {
        Tokenizer tokenizer = new Tokenizer("minecraft:*_log");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(2, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("minecraft:*_log", tokens.get(0).getValue());
    }

    @Test
    void testExpressionWithParentheses() {
        Tokenizer tokenizer = new Tokenizer("(minecraft:stone > 10)");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(6, tokens.size());
        assertEquals(TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals(TokenType.GT, tokens.get(2).getType());
        assertEquals(TokenType.NUMBER, tokens.get(3).getType());
        assertEquals(TokenType.RPAREN, tokens.get(4).getType());
        assertEquals(TokenType.EOF, tokens.get(5).getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "minecraft:stone>10",
        "minecraft:stone > 10",
        "minecraft:stone  >  10",
        "  minecraft:stone > 10  "
    })
    void testWhitespaceHandling(String expression) {
        Tokenizer tokenizer = new Tokenizer(expression);
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(4, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.GT, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals(TokenType.EOF, tokens.get(3).getType());
    }

    @Test
    void testWhitespaceOnlyExpression() {
        Tokenizer tokenizer = new Tokenizer("   ");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    void testNotOperator() {
        Tokenizer tokenizer = new Tokenizer("!");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(2, tokens.size());
        assertEquals(TokenType.NOT, tokens.get(0).getType());
        assertEquals("!", tokens.get(0).getValue());
        assertEquals(TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    void testXorOperator() {
        Tokenizer tokenizer = new Tokenizer("^");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(2, tokens.size());
        assertEquals(TokenType.XOR, tokens.get(0).getType());
        assertEquals("^", tokens.get(0).getValue());
        assertEquals(TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    void testNotExpression() {
        Tokenizer tokenizer = new Tokenizer("!(minecraft:diamond > 10)");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(7, tokens.size());
        assertEquals(TokenType.NOT, tokens.get(0).getType());
        assertEquals(TokenType.LPAREN, tokens.get(1).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals(TokenType.GT, tokens.get(3).getType());
        assertEquals(TokenType.NUMBER, tokens.get(4).getType());
        assertEquals(TokenType.RPAREN, tokens.get(5).getType());
        assertEquals(TokenType.EOF, tokens.get(6).getType());
    }

    @Test
    void testXorExpression() {
        Tokenizer tokenizer = new Tokenizer("a > 10 ^ b > 20");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(8, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.GT, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals(TokenType.XOR, tokens.get(3).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(4).getType());
        assertEquals(TokenType.GT, tokens.get(5).getType());
        assertEquals(TokenType.NUMBER, tokens.get(6).getType());
        assertEquals(TokenType.EOF, tokens.get(7).getType());
    }

    @Test
    void testNotEqualsStillWorks() {
        Tokenizer tokenizer = new Tokenizer("a != 10");
        List<Token> tokens = tokenizer.tokenize();
        
        assertEquals(4, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.NEQ, tokens.get(1).getType());
        assertEquals("!=", tokens.get(1).getValue());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals(TokenType.EOF, tokens.get(3).getType());
    }

}

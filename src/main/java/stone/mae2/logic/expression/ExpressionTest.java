package stone.mae2.logic.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionTest {
    public static void main(String[] args) {
        test("minecraft:stone > 10", true);
        test("minecraft:stone > 100", false);
        test("minecraft:stone == 64", true);
        test("minecraft:dirt > 10", false); // dirt is 0
        test("minecraft:stone > 10 || minecraft:dirt > 10", true);
        test("minecraft:stone > 10 && minecraft:dirt > 10", false);
        test("(minecraft:stone > 10)", true);
        test("mine*:*log > 5", true); // wildcard test
    }

    private static void test(String expression, boolean expected) {
        try {
            Tokenizer tokenizer = new Tokenizer(expression);
            List<Token> tokens = tokenizer.tokenize();
            ExpressionParser parser = new ExpressionParser(tokens);
            Node node = parser.parse();

            InventoryContext context = new MockContext();
            Evaluator evaluator = new Evaluator(context);
            boolean result = evaluator.evaluate(node);

            if (result == expected) {
                System.out.println("[PASS] " + expression + " -> " + result);
            } else {
                System.out.println("[FAIL] " + expression + " -> " + result + " (Expected: " + expected + ")");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + expression + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class MockContext implements InventoryContext {
        private final Map<String, Long> inventory = new HashMap<>();

        public MockContext() {
            inventory.put("minecraft:stone", 64L);
            inventory.put("minecraft:oak_log", 10L);
            inventory.put("minecraft:birch_log", 5L);
        }

        @Override
        public long getCount(String tag) {
            // Simple wildcard support for testing
            if (tag.contains("*")) {
                long sum = 0;
                String regex = tag.replace("*", ".*");
                for (Map.Entry<String, Long> entry : inventory.entrySet()) {
                    if (entry.getKey().matches(regex)) {
                        sum += entry.getValue();
                    }
                }
                return sum;
            }
            return inventory.getOrDefault(tag, 0L);
        }
    }
}

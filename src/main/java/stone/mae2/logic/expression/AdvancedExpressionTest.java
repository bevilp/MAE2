package stone.mae2.logic.expression;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Comprehensive test suite for the Advanced Level Emitter expression system.
 * Tests focus on:
 * - Multi-tag items (items with multiple tags)
 * - Double-counting prevention
 * - Wildcard matching
 * - Complex logical expressions
 */
public class AdvancedExpressionTest {

    public static void main(String[] args) {
        System.out.println("=== Advanced Level Emitter Expression Tests ===\n");

        int passed = 0;
        int failed = 0;

        // Basic tests
        passed += test("Basic: minecraft:stone > 10", "minecraft:stone > 10", true) ? 1 : 0;
        failed += test("Basic: minecraft:stone > 10", "minecraft:stone > 10", true) ? 0 : 1;

        passed += test("Basic: minecraft:stone > 100", "minecraft:stone > 100", false) ? 1 : 0;
        failed += test("Basic: minecraft:stone > 100", "minecraft:stone > 100", false) ? 0 : 1;

        // Multi-tag item tests (CRITICAL - prevents double counting)
        passed += test("Multi-tag: oak_log by ID", "minecraft:oak_log > 5", true) ? 1 : 0;
        failed += test("Multi-tag: oak_log by ID", "minecraft:oak_log > 5", true) ? 0 : 1;

        passed += test("Multi-tag: oak_log by logs tag", "minecraft:logs > 5", true) ? 1 : 0;
        failed += test("Multi-tag: oak_log by logs tag", "minecraft:logs > 5", true) ? 0 : 1;

        passed += test("Multi-tag: oak_log by oak_logs tag", "minecraft:oak_logs > 5", true) ? 1 : 0;
        failed += test("Multi-tag: oak_log by oak_logs tag", "minecraft:oak_logs > 5", true) ? 0 : 1;

        // CRITICAL: Wildcard should NOT double-count items with multiple matching tags
        // oak_log has tags [logs, oak_logs], birch_log has tags [logs, birch_logs]
        // *logs matches both "logs" and "oak_logs" and "birch_logs"
        // But each item should only be counted ONCE per pattern
        // oak_log (10) + birch_log (15) = 25 total
        passed += test("NO DOUBLE COUNT: *logs wildcard", "minecraft:*logs > 5", true) ? 1 : 0;
        failed += test("NO DOUBLE COUNT: *logs wildcard", "minecraft:*logs > 5", true) ? 0 : 1;

        passed += test("NO DOUBLE COUNT: *logs should be 25", "minecraft:*logs == 25", true) ? 1 : 0;
        failed += test("NO DOUBLE COUNT: *logs should be 25", "minecraft:*logs == 25", true) ? 0 : 1;

        passed += test("NO DOUBLE COUNT: *logs should NOT be 50", "minecraft:*logs == 50", false) ? 1 : 0;
        failed += test("NO DOUBLE COUNT: *logs should NOT be 50", "minecraft:*logs == 50", false) ? 0 : 1;

        // Test with multiple items matching wildcard
        passed += test("Wildcard: all logs combined", "minecraft:*log > 15", true) ? 1 : 0;
        failed += test("Wildcard: all logs combined", "minecraft:*log > 15", true) ? 0 : 1;

        passed += test("Wildcard: all logs == 25", "minecraft:*log == 25", true) ? 1 : 0;
        failed += test("Wildcard: all logs == 25", "minecraft:*log == 25", true) ? 0 : 1;

        // Test item with 3 tags
        passed += test("Triple-tag: diamond by ID", "minecraft:diamond > 5", true) ? 1 : 0;
        failed += test("Triple-tag: diamond by ID", "minecraft:diamond > 5", true) ? 0 : 1;

        passed += test("Triple-tag: diamond by gems tag", "minecraft:gems > 5", true) ? 1 : 0;
        failed += test("Triple-tag: diamond by gems tag", "minecraft:gems > 5", true) ? 0 : 1;

        passed += test("Triple-tag: diamond by valuables tag", "minecraft:valuables > 5", true) ? 1 : 0;
        failed += test("Triple-tag: diamond by valuables tag", "minecraft:valuables > 5", true) ? 0 : 1;

        passed += test("Triple-tag: diamond by diamonds tag", "minecraft:diamonds > 5", true) ? 1 : 0;
        failed += test("Triple-tag: diamond by diamonds tag", "minecraft:diamonds > 5", true) ? 0 : 1;

        // CRITICAL: Wildcard matching multiple tags of same item should NOT double
        // count
        passed += test("NO DOUBLE COUNT: *mond wildcard", "minecraft:*mond == 8", true) ? 1 : 0;
        failed += test("NO DOUBLE COUNT: *mond wildcard", "minecraft:*mond == 8", true) ? 0 : 1;

        passed += test("NO DOUBLE COUNT: *mond should NOT be 24", "minecraft:*mond == 24", false) ? 1 : 0;
        failed += test("NO DOUBLE COUNT: *mond should NOT be 24", "minecraft:*mond == 24", false) ? 0 : 1;

        // Logical operators with multi-tag items
        passed += test("Logic: oak_log AND diamond", "minecraft:oak_log > 5 && minecraft:diamond > 5", true) ? 1 : 0;
        failed += test("Logic: oak_log AND diamond", "minecraft:oak_log > 5 && minecraft:diamond > 5", true) ? 0 : 1;

        passed += test("Logic: logs OR gems", "minecraft:logs > 100 || minecraft:gems > 5", true) ? 1 : 0;
        failed += test("Logic: logs OR gems", "minecraft:logs > 100 || minecraft:gems > 5", true) ? 0 : 1;

        // Complex expressions
        passed += test("Complex: (logs > 5 && gems > 5) || stone > 100",
                "(minecraft:logs > 5 && minecraft:gems > 5) || minecraft:stone > 100", true) ? 1 : 0;
        failed += test("Complex: (logs > 5 && gems > 5) || stone > 100",
                "(minecraft:logs > 5 && minecraft:gems > 5) || minecraft:stone > 100", true) ? 0 : 1;

        // Edge cases
        passed += test("Edge: non-existent item", "minecraft:nonexistent > 0", false) ? 1 : 0;
        failed += test("Edge: non-existent item", "minecraft:nonexistent > 0", false) ? 0 : 1;

        passed += test("Edge: exact match on 0", "minecraft:nonexistent == 0", true) ? 1 : 0;
        failed += test("Edge: exact match on 0", "minecraft:nonexistent == 0", true) ? 0 : 1;

        System.out.println("\n=== Test Summary ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));

        if (failed == 0) {
            System.out.println("\n[SUCCESS] ALL TESTS PASSED!");
        } else {
            System.out.println("\n[FAILURE] SOME TESTS FAILED!");
            System.exit(1);
        }
    }

    private static boolean test(String testName, String expression, boolean expected) {
        try {
            Tokenizer tokenizer = new Tokenizer(expression);
            List<Token> tokens = tokenizer.tokenize();
            ExpressionParser parser = new ExpressionParser(tokens);
            Node node = parser.parse();

            // Simulate AE2 inventory context with proper multi-tag support
            InventoryContext context = new SimulatedAE2Context();
            Evaluator evaluator = new Evaluator(context);
            boolean result = evaluator.evaluate(node);

            if (result == expected) {
                System.out.println("[PASS] " + testName);
                return true;
            } else {
                System.out.println("[FAIL] " + testName);
                System.out.println("       Expression: " + expression);
                System.out.println("       Expected: " + expected + ", Got: " + result);
                return false;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + testName);
            System.out.println("        Expression: " + expression);
            System.out.println("        Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Simulates the AE2 inventory context with proper multi-tag support.
     * This mimics how the actual AdvancedLevelEmitterPart.AE2InventoryContext
     * works.
     */
    static class SimulatedAE2Context implements InventoryContext {
        private final List<InventoryItem> items = new ArrayList<>();
        private final Map<String, Long> cachedCounts = new HashMap<>();
        private final Set<String> requestedPatterns = new HashSet<>();

        public SimulatedAE2Context() {
            // Item with 1 tag (just ID)
            items.add(new InventoryItem("minecraft:stone", 64, List.of()));

            // Item with 2 tags (ID + logs, ID + oak_logs)
            items.add(new InventoryItem("minecraft:oak_log", 10,
                    List.of("minecraft:logs", "minecraft:oak_logs")));

            // Item with 2 tags (ID + logs, ID + birch_logs)
            items.add(new InventoryItem("minecraft:birch_log", 15,
                    List.of("minecraft:logs", "minecraft:birch_logs")));

            // Item with 3 tags (ID + gems, ID + valuables, ID + diamonds)
            items.add(new InventoryItem("minecraft:diamond", 8,
                    List.of("minecraft:gems", "minecraft:valuables", "minecraft:diamonds")));
        }

        @Override
        public long getCount(String pattern) {
            // Track which patterns are requested
            requestedPatterns.add(pattern);

            // Check cache first
            if (cachedCounts.containsKey(pattern)) {
                return cachedCounts.get(pattern);
            }

            // Scan inventory for this pattern
            long count = scanForPattern(pattern);
            cachedCounts.put(pattern, count);
            return count;
        }

        private long scanForPattern(String pattern) {
            Pattern regex = null;
            if (pattern.contains("*")) {
                regex = Pattern.compile(pattern.replace("*", ".*"));
            }

            long totalCount = 0;

            // For each item in inventory
            for (InventoryItem item : items) {
                // Collect all identifiers for this item (ID + tags)
                Set<String> identifiers = new HashSet<>();
                identifiers.add(item.id);
                identifiers.addAll(item.tags);

                // Check if this item matches the pattern
                boolean matches = false;
                if (regex != null) {
                    // Wildcard pattern - check if any identifier matches
                    for (String identifier : identifiers) {
                        if (regex.matcher(identifier).matches()) {
                            matches = true;
                            break;
                        }
                    }
                } else {
                    // Exact match
                    matches = identifiers.contains(pattern);
                }

                // If matches, add quantity ONCE (not once per tag!)
                if (matches) {
                    totalCount += item.quantity;
                }
            }

            return totalCount;
        }

        static class InventoryItem {
            final String id;
            final long quantity;
            final List<String> tags;

            InventoryItem(String id, long quantity, List<String> tags) {
                this.id = id;
                this.quantity = quantity;
                this.tags = tags;
            }
        }
    }
}

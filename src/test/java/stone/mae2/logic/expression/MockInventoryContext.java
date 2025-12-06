package stone.mae2.logic.expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of InventoryContext for testing expression evaluation.
 * Tests should explicitly set up their inventory using setCount().
 */
public class MockInventoryContext implements InventoryContext {
    private final Map<String, Long> inventory = new HashMap<>();

    /**
     * Set a specific item count for testing.
     */
    public void setCount(String identifier, long count) {
        inventory.put(identifier, count);
    }

    /**
     * Clear the inventory for testing.
     */
    public void clear() {
        inventory.clear();
    }

    @Override
    public long getCount(String tag) {
        // Support wildcard patterns
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
        
        // Exact match
        return inventory.getOrDefault(tag, 0L);
    }
}

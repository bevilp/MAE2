package stone.mae2.parts.automation;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.api.util.IConfigManager;
import appeng.api.stacks.KeyCounter;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.automation.AbstractLevelEmitterPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import stone.mae2.MAE2;
import stone.mae2.logic.expression.Evaluator;
import stone.mae2.logic.expression.ExpressionParser;
import stone.mae2.logic.expression.InventoryContext;
import stone.mae2.logic.expression.Node;
import stone.mae2.logic.expression.Tokenizer;
import stone.mae2.logic.expression.Token;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import stone.mae2.logic.expression.BinaryNode;
import stone.mae2.logic.expression.TagNode;
import stone.mae2.menu.AdvancedLevelEmitterMenu;

public class AdvancedLevelEmitterPart extends AbstractLevelEmitterPart {

    // Reuse AE2's level emitter models - two-part system: base + status
    @PartModels
    private static final ResourceLocation MODEL_BASE_OFF = new ResourceLocation("ae2", "part/level_emitter_base_off");
    @PartModels
    private static final ResourceLocation MODEL_BASE_ON = new ResourceLocation("ae2", "part/level_emitter_base_on");
    @PartModels
    private static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation("ae2", "part/level_emitter_status_off");
    @PartModels
    private static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation("ae2", "part/level_emitter_status_on");
    @PartModels
    private static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation("ae2", "part/level_emitter_status_has_channel");

    private static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
    private static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
    private static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    private static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
    private static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
    private static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);

    @PartModels
    public static List<IPartModel> getModels() {
        return List.of(MODEL_OFF_OFF, MODEL_OFF_ON, MODEL_OFF_HAS_CHANNEL,
                       MODEL_ON_OFF, MODEL_ON_ON, MODEL_ON_HAS_CHANNEL);
    }

    private String expression = "";
    private Node parsedExpression;
    private boolean lastRedstoneState;
    private final IActionSource source;
    private String lastErrorMessage; // Track last error to prevent spam
    private boolean hasCompletedFirstTick = false; // Skip first tick after addToWorld to ensure grid is ready

    public AdvancedLevelEmitterPart(IPartItem<?> partItem) {
        super(partItem);
        this.source = new MachineSource(this);
        
        // Register as tickable service with fixed 20-tick rate
        getMainNode().addService(IGridTickable.class, new IGridTickable() {
            @Override
            public TickingRequest getTickingRequest(IGridNode node) {
                // Fixed 20-tick interval (once per second at 20 TPS)
                return new TickingRequest(20, 20, false, false);
            }
            
            @Override
            public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
                // Skip first tick after addToWorld() to ensure grid/storage is fully initialized
                // This prevents flickering on world load when cached inventory isn't ready yet
                if (!hasCompletedFirstTick) {
                    hasCompletedFirstTick = true;
                    MAE2.LOGGER.debug("First tick after addToWorld - skipping evaluation, maintaining loaded state");
                    return TickRateModulation.SAME;
                }
                
                MAE2.LOGGER.debug("AdvancedLevelEmitterPart tick called - ticksSinceLastCall: {}", ticksSinceLastCall);
                updateRedstoneOutput();
                return TickRateModulation.SAME;  // Keep constant 20-tick rate
            }
        });
    }

    @Override
    protected int getUpgradeSlots() {
        return 0; // Advanced level emitter doesn't use upgrade cards
    }

    @Override
    public void upgradesChanged() {
        // No upgrades to handle
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        MAE2.LOGGER.info("AdvancedLevelEmitterPart addToWorld called - will skip first tick evaluation");
        // Reset flag when added to world - ensures we skip first tick evaluation
        // This keeps the part in its loaded state until grid/storage is confirmed ready
        hasCompletedFirstTick = false;
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        MAE2.LOGGER.info("AdvancedLevelEmitterPart removeFromWorld called");
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.expression = data.getString("expression");
        this.lastRedstoneState = data.getBoolean("lastRedstoneState");
        parseExpression();
        
        MAE2.LOGGER.info("AdvancedLevelEmitterPart readFromNBT - expression: {}, lastRedstoneState: {}", 
            this.expression, this.lastRedstoneState);
    }
    
    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        // Ensure the visual state matches our lastRedstoneState
        data.putBoolean("on", isLevelEmitterOn());
    }
    
    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        // Sync our internal state with the visual state on load
        // This helps prevent flicker by ensuring consistency
        boolean savedState = data.getBoolean("on");
        MAE2.LOGGER.debug("readVisualStateFromNBT - savedState: {}", savedState);
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putString("expression", this.expression);
        data.putBoolean("lastRedstoneState", this.lastRedstoneState);
        MAE2.LOGGER.info("AdvancedLevelEmitterPart writeToNBT - expression: {}, lastRedstoneState: {}", 
            this.expression, this.lastRedstoneState);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        MAE2.LOGGER.info("onPartActivate called - isClient: {}, isSneaking: {}", 
            player.level().isClientSide(), player.isShiftKeyDown());
        
        if (player.isShiftKeyDown()) {
            return false;
        }
        
        if (!player.level().isClientSide()) {
            MAE2.LOGGER.info("Attempting to open menu. MenuType: {}", getMenuType(player));
            try {
                MenuOpener.open(getMenuType(player), player, MenuLocators.forPart(this));
                MAE2.LOGGER.info("MenuOpener.open completed successfully");
            } catch (Exception e) {
                MAE2.LOGGER.error("Failed to open menu", e);
            }
        }
        return true;
    }

    public MenuType<?> getMenuType(Player player) {
        return AdvancedLevelEmitterMenu.TYPE;
    }

    @Override
    protected boolean hasDirectOutput() {
        // We use IGridTickable for ticking, not the direct output polling system
        return false;
    }

    /**
     * Called every 20 ticks by the grid tick manager to evaluate the expression
     * and update redstone output if needed.
     */
    private void updateRedstoneOutput() {
        if (parsedExpression == null) {
            MAE2.LOGGER.debug("updateRedstoneOutput: No expression configured");
            return;
        }

        IGridNode node = getGridNode();
        if (node == null || !node.isActive()) {
            MAE2.LOGGER.debug("updateRedstoneOutput: Node null or inactive");
            return;
        }

        IStorageService storageService = node.getGrid().getService(IStorageService.class);
        if (storageService == null) {
            MAE2.LOGGER.debug("updateRedstoneOutput: No storage service");
            return;
        }
        
        // Check if storage service has inventory available
        KeyCounter inventory = storageService.getCachedInventory();
        if (inventory == null) {
            MAE2.LOGGER.debug("updateRedstoneOutput: No cached inventory available");
            return;
        }

        InventoryContext context = new AE2InventoryContext(storageService);
        try {
            Evaluator evaluator = new Evaluator(context);
            boolean result = evaluator.evaluate(parsedExpression);
            
            // Clear error state on successful evaluation
            lastErrorMessage = null;
            
            MAE2.LOGGER.debug("updateRedstoneOutput: Evaluated to {} (previous: {})", result, lastRedstoneState);
            
            // Update the state if it changed
            if (result != lastRedstoneState) {
                lastRedstoneState = result;
                updateState();  // This triggers the redstone update
                MAE2.LOGGER.info("Redstone state changed to: {}", result);
            }
        } catch (Exception e) {
            // Only log if this is a new error or different from the last one
            String errorMessage = e.getMessage();
            if (lastErrorMessage == null || !lastErrorMessage.equals(errorMessage)) {
                MAE2.LOGGER.error("Failed to evaluate expression '{}': {}", expression, errorMessage);
                lastErrorMessage = errorMessage;
            }
        }
    }

    @Override
    protected boolean isLevelEmitterOn() {
        if (isClientSide()) {
            return super.isLevelEmitterOn();
        }
        
        // Return false if no expression is configured (prevents default emission)
        if (parsedExpression == null || expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        if (!this.getMainNode().isActive()) {
            return false;
        }
        
        // Check if we should invert the output based on redstone mode
        final boolean flipState = this.getConfigManager()
                .getSetting(Settings.REDSTONE_EMITTER) == RedstoneMode.LOW_SIGNAL;
        
        // Return the cached evaluation state, inverted if in LOW_SIGNAL mode
        return flipState ? !lastRedstoneState : lastRedstoneState;
    }

    @Override
    protected boolean getDirectOutput() {
        // Return cached state - this is called when checking redstone output
        // The actual evaluation happens in updateRedstoneOutput() every 20 ticks
        return lastRedstoneState;
    }

    public void setExpression(String expression) {
        this.expression = expression;
        lastErrorMessage = null; // Clear error state when expression changes
        parseExpression();
        getHost().markForSave();
    }

    public String getExpression() {
        return this.expression;
    }

    private void parseExpression() {
        try {
            if (expression == null || expression.trim().isEmpty()) {
                this.parsedExpression = null;
                return;
            }
            Tokenizer tokenizer = new Tokenizer(expression);
            List<Token> tokens = tokenizer.tokenize();
            ExpressionParser parser = new ExpressionParser(tokens);
            this.parsedExpression = parser.parse();
        } catch (Exception e) {
            this.parsedExpression = null;
            MAE2.LOGGER.error("Failed to parse expression: {}", expression, e);
        }
    }

    @Override
    protected void configureWatchers() {
        // Advanced emitter doesn't use watchers - it evaluates via 20-tick cycle
        // Always call updateState to maintain visual consistency
        updateState();
    }

    @Override
    public IPartModel getStaticModels() {
        // Match AE2's StorageLevelEmitterPart logic
        if (this.isActive() && this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_ON : MODEL_OFF_ON;
        } else {
            return this.isLevelEmitterOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
        }
    }

    /**
     * Single-pass inventory scanner that correctly aggregates tag counts.
     * Each item is counted once per unique tag pattern in the expression.
     * Reuses collections to minimize GC pressure.
     */
    private class AE2InventoryContext implements InventoryContext {
        private final Map<String, Long> aggregatedCounts = new HashMap<>();
        private final Set<String> tagPatterns = new HashSet<>();
        private final Map<String, Pattern> compiledPatterns = new HashMap<>();
        private final Set<String> itemIdentifiers = new HashSet<>();
        private final IStorageService storageService;
        private boolean scanned = false;

        public AE2InventoryContext(IStorageService storageService) {
            this.storageService = storageService;
        }

        @Override
        public long getCount(String tagPattern) {
            if (!scanned) {
                scanInventory();
                scanned = true;
            }
            return aggregatedCounts.getOrDefault(tagPattern, 0L);
        }

        private void scanInventory() {
            if (parsedExpression == null) {
                return;
            }

            // Clear and reuse collections
            aggregatedCounts.clear();
            tagPatterns.clear();
            compiledPatterns.clear();

            // Collect all unique tag patterns from the expression
            collectTagPatterns(parsedExpression, tagPatterns);

            // Compile wildcard patterns once
            for (String pattern : tagPatterns) {
                if (pattern.contains("*")) {
                    String regex = pattern.replace("*", ".*");
                    compiledPatterns.put(pattern, Pattern.compile(regex));
                }
            }

            // Single pass through inventory
            KeyCounter inventory = storageService.getCachedInventory();
            for (var entry : inventory) {
                AEKey key = entry.getKey();
                long amount = entry.getLongValue();
                ResourceLocation itemId = key.getId();

                // Reuse itemIdentifiers set - contains item ID + all tag locations
                itemIdentifiers.clear();
                itemIdentifiers.add(itemId.toString());
                
                // Get item tags from Minecraft registry (cosmolite-compatible)
                try {
                    Item item = BuiltInRegistries.ITEM.get(itemId);
                    if (item != null) {
                        // Get all tags this item belongs to
                        Stream<TagKey<Item>> tags = item.builtInRegistryHolder().tags();
                        tags.forEach(tagKey -> {
                            // Add tag location as identifier (e.g., "minecraft:logs")
                            itemIdentifiers.add(tagKey.location().toString());
                        });
                    }
                } catch (Exception e) {
                    // If we can't get tags, just use item ID
                    MAE2.LOGGER.debug("Could not get tags for item: {}", itemId, e);
                }

                // For each tag pattern in the expression, check if this item matches
                // Each item is counted at most once per pattern
                for (String pattern : tagPatterns) {
                    boolean matches = false;

                    if (compiledPatterns.containsKey(pattern)) {
                        // Wildcard pattern - check if any identifier matches
                        Pattern regex = compiledPatterns.get(pattern);
                        for (String identifier : itemIdentifiers) {
                            if (regex.matcher(identifier).matches()) {
                                matches = true;
                                break;
                            }
                        }
                    } else {
                        // Exact match against item ID or any tag
                        matches = itemIdentifiers.contains(pattern);
                    }

                    if (matches) {
                        aggregatedCounts.put(pattern,
                                aggregatedCounts.getOrDefault(pattern, 0L) + amount);
                    }
                }
            }
        }

        private void collectTagPatterns(Node node, Set<String> patterns) {
            if (node instanceof TagNode) {
                patterns.add(((TagNode) node).getTag());
            } else if (node instanceof BinaryNode) {
                BinaryNode binary = (BinaryNode) node;
                collectTagPatterns(binary.getLeft(), patterns);
                collectTagPatterns(binary.getRight(), patterns);
            }
            // LiteralNode doesn't contain tag patterns
        }
    }
}

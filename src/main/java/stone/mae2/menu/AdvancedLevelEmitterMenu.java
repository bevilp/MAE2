package stone.mae2.menu;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.menu.implementations.UpgradeableMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import stone.mae2.bootstrap.MAE2Menus;
import stone.mae2.parts.automation.AdvancedLevelEmitterPart;

public class AdvancedLevelEmitterMenu extends UpgradeableMenu<AdvancedLevelEmitterPart> {

    private static final String ACTION_SET_EXPRESSION = "setExpression";

    public static final MenuType<AdvancedLevelEmitterMenu> TYPE = MAE2Menus.ADVANCED_LEVEL_EMITTER.get();

    // Only synced once on menu-open, and only used on client
    private String expression = "";

    public AdvancedLevelEmitterMenu(MenuType<AdvancedLevelEmitterMenu> menuType, int id, Inventory ip,
            AdvancedLevelEmitterPart host) {
        super(menuType, id, ip, host);
        
        registerClientAction(ACTION_SET_EXPRESSION, String.class, this::setExpression);
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String newExpression) {
        if (isClientSide()) {
            if (!newExpression.equals(this.expression)) {
                this.expression = newExpression;
                sendClientAction(ACTION_SET_EXPRESSION, newExpression);
            }
        } else {
            getHost().setExpression(newExpression);
        }
    }

    /**
     * Called from network initialization to set the expression received from the server.
     * This is separate from setExpression() to avoid triggering a client action during menu initialization.
     */
    public void setExpressionFromNetwork(String expression) {
        this.expression = expression;
    }

    @Override
    protected void setupConfig() {
        // No config inventory for advanced level emitter
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        // Advanced level emitter only supports redstone emitter mode
        // (no fuzzy mode, no crafting mode, no scheduling mode)
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_EMITTER));
    }
}

package stone.mae2.client.gui;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import stone.mae2.menu.AdvancedLevelEmitterMenu;

public class AdvancedLevelEmitterScreen extends UpgradeableScreen<AdvancedLevelEmitterMenu> {

    private final AETextField expressionField;
    private final SettingToggleButton<RedstoneMode> redstoneMode;

    public AdvancedLevelEmitterScreen(AdvancedLevelEmitterMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        
        this.redstoneMode = new ServerSettingToggleButton<>(Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.addToLeftToolbar(this.redstoneMode);
        
        // Initialize text field in constructor like AE2 does
        this.expressionField = widgets.addTextField("expressionField");
        this.expressionField.setMaxLength(256);
        this.expressionField.setValue(menu.getExpression());
        this.expressionField.setResponder(menu::setExpression);
    }

    @Override
    protected void init() {
        super.init();
        this.setInitialFocus(this.expressionField);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        
        // Update the redstone mode button to reflect current setting
        this.redstoneMode.set(menu.getRedStoneMode());
        this.redstoneMode.setVisibility(true);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX,
            int mouseY, float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Right-click to clear
        if (button == 1 && this.expressionField.isMouseOver(mouseX, mouseY)) {
            this.expressionField.setValue("");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

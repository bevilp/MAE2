package stone.mae2.bootstrap;

import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import stone.mae2.MAE2;
import stone.mae2.menu.AdvancedLevelEmitterMenu;
import stone.mae2.parts.automation.AdvancedLevelEmitterPart;

public abstract class MAE2Menus {

    static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister
            .create(ForgeRegistries.MENU_TYPES, MAE2.MODID);

    public static final RegistryObject<MenuType<AdvancedLevelEmitterMenu>> ADVANCED_LEVEL_EMITTER = MENUS.register(
            "advanced_level_emitter",
            () -> MenuTypeBuilder
                    .create(AdvancedLevelEmitterMenu::new, AdvancedLevelEmitterPart.class)
                    .withInitialData((host, buffer) -> {
                        // Send expression to client when menu opens
                        buffer.writeUtf(host.getExpression());
                    }, (host, menu, buffer) -> {
                        // Receive expression on client
                        menu.setExpressionFromNetwork(buffer.readUtf());
                    })
                    .build("advanced_level_emitter"));

    public static void init(IEventBus bus) {
        MENUS.register(bus);
    }
}

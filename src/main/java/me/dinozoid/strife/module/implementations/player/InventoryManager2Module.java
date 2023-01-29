package me.dinozoid.strife.module.implementations.player;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.player.WindowClickEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "InventoryManager2", renderName = "InventoryManager2", category = Category.PLAYER)
public class InventoryManager2Module extends Module {

    private final EnumProperty<InventoryMode> inventoryModeProperty = new EnumProperty<>("Mode", InventoryMode.SPOOF);
    private final Property<Boolean> cleanProperty = new Property("Clean", true);
    private final Property<Boolean> sortProperty = new Property("Sort", true);
    private final Property<Boolean> autoArmorProperty = new Property("Auto Armor", true);
    private final Property<Boolean> whileFightingProperty = new Property("While Fighting", false);
    private final DoubleProperty clickDelayProperty = new DoubleProperty("Click Delay", 50, 0, 300, 10, Property.Representation.MILLISECONDS);
    private final DoubleProperty blocksProperty = new DoubleProperty("Blocks", 64, 0, 512, 64, Property.Representation.INT);
    private final DoubleProperty arrowsProperty = new DoubleProperty("Arrows", 64, 0, 512, 64, Property.Representation.INT);

    private final TimerUtil interactionsTimer = new TimerUtil();
    private boolean spoofOpened;

    // cry bro, I don't want to have to check millis manually :skull:
    @EventHandler
    private final Listener<WindowClickEvent> windowClickListener = new Listener<>(event -> interactionsTimer.reset());

    @EventHandler
    private final Listener<PacketOutboundEvent> packetOutboundListener = new Listener<>(event -> {
        if (inventoryModeProperty.getValue() == InventoryMode.SPOOF && spoofOpened) {
            if (event.getPacket() instanceof C16PacketClientStatus)
                event.cancel();
            if (event.getPacket() instanceof C0DPacketCloseWindow) {
                C0DPacketCloseWindow packet = event.getPacket();
                event.setCancelled(packet.getWindowId() == mc.thePlayer.inventoryContainer.windowId);
            }
        }
    });

    @EventHandler
    private final Listener<PlayerMotionEvent> motionEventListener = new Listener<>(event -> {
        boolean shouldBeOpen = inventoryModeProperty.getValue() == InventoryMode.SPOOF ||
                (inventoryModeProperty.getValue() == InventoryMode.OPEN && mc.currentScreen instanceof GuiInventory)
                || inventoryModeProperty.getValue() == InventoryMode.NONE;

        if (!shouldBeOpen) return;

        boolean fighting = mc.objectMouseOver != null &&
                mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY &&
                mc.objectMouseOver.entityHit.hurtResistantTime >= 10;

        if (fighting && !whileFightingProperty.getValue()) return;

        int bestSwordSlot = -1,
                bestPickaxeSlot = -1,
                bestShovelSlot = -1,
                bestAxeSlot = -1,
                bestFoodSlot = -1,
                bestBowSlot = -1,
                bestPotionSlot = -1;
        float bestSwordDamage = -1,
                bestPickaxeDamage = -1,
                bestShovelDamage = -1,
                bestAxeDamage = -1,
                bestFoodDamage = -1,
                bestBowDamage = -1,
                bestPotionDamage = -1,
                totalBlockSize = 0,
                totalArrowSize = 0;

        int[] bestArmorSlots = new int[4];
        float[] bestArmorDamage = new float[4];
        Arrays.fill(bestArmorSlots, -1);
        Arrays.fill(bestArmorDamage, -1);

        final List<ItemStack> blocks = new ArrayList<>();
        final List<ItemStack> arrows = new ArrayList<>();
        final List<ItemStack> duplicates = new ArrayList<>();

        for (int slot = PlayerUtil.INCLUDE_ARMOR_BEGIN; slot < PlayerUtil.END; slot++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
            if (stack == null) continue;
            final float damage = PlayerUtil.getItemDamage(stack);
            if (damage > 0) {
                if (stack.getItem() instanceof ItemSword) {
                    if(bestSwordSlot != -1 && bestSwordDamage == damage) {
                        final ItemStack bestStack = mc.thePlayer.inventoryContainer.getSlot(bestSwordSlot).getStack();
                        final float bestDurability = bestStack.getMaxDamage() - bestStack.getItemDamage(),
                                        durability = stack.getMaxDamage() - stack.getItemDamage();
                        if(durability != 0 && bestDurability <= durability)
                            click(slot, 1, PlayerUtil.ClickType.DROP_ITEM.ordinal());
                        continue;
                    }
                    if (bestSwordDamage < damage) {
                        bestSwordDamage = damage;
                        bestSwordSlot = slot;
                    }
                }
                if (stack.getItem() instanceof ItemBow) {
                    if(bestBowSlot != -1 && bestBowDamage == damage) {
                        final ItemStack bestStack = mc.thePlayer.inventoryContainer.getSlot(bestBowSlot).getStack();
                        final float bestDurability = bestStack.getMaxDamage() - bestStack.getItemDamage(),
                                durability = stack.getMaxDamage() - stack.getItemDamage();
                        if(durability != 0 && bestDurability <= durability)
                            click(slot, 1, PlayerUtil.ClickType.DROP_ITEM.ordinal());
                        continue;
                    }
                    if (bestBowDamage < damage) {
                        bestBowDamage = damage;
                        bestBowSlot = slot;
                    }
                }
                if (stack.getItem() instanceof ItemPickaxe) {
                    if(bestPickaxeSlot != -1 && bestPickaxeDamage == damage) {
                        final ItemStack bestStack = mc.thePlayer.inventoryContainer.getSlot(bestPickaxeSlot).getStack();
                        final float bestDurability = bestStack.getMaxDamage() - bestStack.getItemDamage(),
                                durability = stack.getMaxDamage() - stack.getItemDamage();
                        if(durability != 0 && bestDurability <= durability)
                            click(slot, 1, PlayerUtil.ClickType.DROP_ITEM.ordinal());
                        continue;
                    }
                    if (bestPickaxeDamage < damage) {
                        bestPickaxeDamage = damage;
                        bestPickaxeSlot = slot;
                    }
                }
                if (stack.getItem() instanceof ItemAxe) {
                    if(bestAxeSlot != -1 && bestAxeDamage == damage) {
                        final ItemStack bestStack = mc.thePlayer.inventoryContainer.getSlot(bestAxeSlot).getStack();
                        final float bestDurability = bestStack.getMaxDamage() - bestStack.getItemDamage(),
                                durability = stack.getMaxDamage() - stack.getItemDamage();
                        if(durability != 0 && bestDurability <= durability)
                            click(slot, 1, PlayerUtil.ClickType.DROP_ITEM.ordinal());
                        continue;
                    }
                    if (bestAxeDamage < damage) {
                        bestAxeDamage = damage;
                        bestAxeSlot = slot;
                    }
                }
                if (stack.getItem() instanceof ItemSpade) {
                    if(bestShovelSlot != -1 && bestShovelDamage == damage) {
                        final ItemStack bestStack = mc.thePlayer.inventoryContainer.getSlot(bestShovelSlot).getStack();
                        final float bestDurability = bestStack.getMaxDamage() - bestStack.getItemDamage(),
                                durability = stack.getMaxDamage() - stack.getItemDamage();
                        if(durability != 0 && bestDurability <= durability)
                            click(slot, 1, PlayerUtil.ClickType.DROP_ITEM.ordinal());
                        continue;
                    }
                    if (bestShovelDamage < damage) {
                        bestShovelDamage = damage;
                        bestShovelSlot = slot;
                    }
                }
                if (stack.getItem() instanceof ItemFood && stack.getItem() != Items.golden_apple) {
                    if (bestFoodDamage < damage) {
                        bestFoodDamage = damage;
                        bestFoodSlot = slot;
                    }
                }
                if (stack.getItem() instanceof ItemPotion) {
                    if (bestPotionDamage < damage) {
                        bestPotionDamage = damage;
                        bestPotionSlot = slot;
                    }
                }
                if (stack.getItem() instanceof ItemArmor) {
                    ItemArmor armor = (ItemArmor) stack.getItem();
                    if (bestArmorDamage[armor.armorType] < damage) {
                        bestArmorDamage[armor.armorType] = damage;
                        bestArmorSlots[armor.armorType] = slot;
                    }
                }
                if (stack.getItem() instanceof ItemBlock) {
                    blocks.sort(Comparator.comparingInt(b -> ((ItemStack) b).stackSize).reversed());
                    totalBlockSize += stack.stackSize;
                    int size = MathHelper.ceiling_float_int(blocksProperty.getValue().floatValue() / 64);
                    if ((totalBlockSize % 64 == 0 && blocks.size() < size || stack.stackSize > 0 && stack.stackSize < 64 && totalBlockSize < blocksProperty.getValue()))
                        blocks.add(stack);
                }
            }
            if(stack.getItem() == Items.arrow) {
                totalArrowSize += stack.stackSize;
                if ((totalArrowSize % 64 == 0 || stack.stackSize > 0 && stack.stackSize < 64) &&
                        arrows.size() < MathHelper.ceiling_float_int(arrowsProperty.getValue().floatValue() / 64))
                    arrows.add(stack);
            }
        }

        if (autoArmorProperty.getValue()) {
            for (int i = 0; i < bestArmorSlots.length; i++) {
                final int slot = bestArmorSlots[i];
                if (slot != -1 && interactionsTimer.hasElapsed(clickDelayProperty.getValue().longValue()) &&
                        mc.thePlayer.inventoryContainer.getSlot(i + PlayerUtil.INCLUDE_ARMOR_BEGIN).getStack() == null)
                    click(slot, 0, PlayerUtil.ClickType.SHIFT_CLICK.ordinal());
            }
        }

        final int[] slots = { bestSwordSlot, bestBowSlot, bestPickaxeSlot, bestAxeSlot, bestShovelSlot, bestPotionSlot, bestFoodSlot };
        if (sortProperty.getValue()) {
            int currentSlot = PlayerUtil.ONLY_HOT_BAR_BEGIN;
            for (int item : slots) {
                if (item != -1) {
                    if (item != currentSlot && interactionsTimer.hasElapsed(clickDelayProperty.getValue().longValue())) {
                        click(item, currentSlot - PlayerUtil.ONLY_HOT_BAR_BEGIN, PlayerUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT.ordinal());
                    }
                    currentSlot++;
                }
            }
        }
        if (cleanProperty.getValue()) {
            for (int slot = PlayerUtil.INCLUDE_ARMOR_BEGIN; slot < PlayerUtil.END; slot++) {
                final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
                if (stack == null) continue;
                final float damage = PlayerUtil.getItemDamage(stack);
                if (interactionsTimer.hasElapsed(clickDelayProperty.getValue().longValue())) {
                    if (stack.getItem() instanceof ItemSword && slot != bestSwordSlot ||
                            stack.getItem() instanceof ItemBow && slot != bestBowSlot ||
                            stack.getItem() instanceof ItemPickaxe && slot != bestPickaxeSlot ||
                            stack.getItem() instanceof ItemAxe && slot != bestAxeSlot ||
                            stack.getItem() instanceof ItemSpade && slot != bestShovelSlot ||
                            stack.getItem() instanceof ItemFood && stack.getItem() != Items.golden_apple && slot != bestFoodSlot ||
                            stack.getItem() instanceof ItemArmor &&
                                    slot != bestArmorSlots[((ItemArmor) stack.getItem()).armorType] ||
                            stack.getItem() == Items.arrow && !arrows.contains(stack) ||
                            duplicates.contains(stack) ||
                        damage == 0 && stack.getItem() != Items.arrow
                    ) click(slot, 1, PlayerUtil.ClickType.DROP_ITEM.ordinal());
                }
            }
        }
        close();
    });

    private void click(int slot, int mode, int button) {
        open();
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, mode, button, mc.thePlayer);
    }

    private void open() {
        if (!spoofOpened && inventoryModeProperty.getValue() == InventoryMode.SPOOF) {
            if (!(mc.currentScreen instanceof GuiInventory))
                PacketUtil.sendPacketNoEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            interactionsTimer.reset();
            spoofOpened = true;
        }
    }

    private void close() {
        if (spoofOpened && inventoryModeProperty.getValue() == InventoryMode.SPOOF) {
            if (!(mc.currentScreen instanceof GuiInventory))
                PacketUtil.sendPacketNoEvent(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            spoofOpened = false;
        }
    }

    public enum InventoryMode {
        SPOOF, OPEN, NONE
    }

    public enum ItemElement {
        SWORD, BOW, PICKAXE, SHOVEL, GOLDEN_APPLE, HEAD, BUCKETS
    }
}

package com.pedro.racasclasses.client.gui;

import com.pedro.racasclasses.attribute.Attribute;
import com.pedro.racasclasses.attribute.AttributeBonus;
import com.pedro.racasclasses.attribute.AttributeData;
import com.pedro.racasclasses.attribute.AttributeRegistry;
import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.network.DistributeAttributePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * HUD de atributos (tecla P). Três páginas estilo livro: trilhas, bônus, guia.
 * Fecha no X, em ESC ou na tecla P de novo.
 */
public class AttributeScreen extends Screen {
    private static final int LEATHER = 0xF018100D;
    private static final int PARCHMENT = 0xFFD8C39C;
    private static final int INK = 0xFF17100D;
    private static final int GOLD = 0xFFC99A45;
    private static final int GOLD_LIGHT = 0xFFF0C96B;
    private static final int MUTED = 0xFFAA9879;
    private static final int MOSS = 0xFF86A276;
    private static final int OXBLOOD = 0xFF642B2D;

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 248;
    private static final int TOTAL_PAGES = 3;
    private static final int PAGE_ATTRIBUTES = 0;
    private static final int PAGE_BONUS = 1;
    private static final int PAGE_GUIDE = 2;

    private int currentPage = 0;
    private final Map<Attribute, Button> plusButtons = new EnumMap<>(Attribute.class);
    private final Map<Attribute, int[]> labelHits = new EnumMap<>(Attribute.class);
    private Button prevButton;
    private Button nextButton;

    public AttributeScreen() {
        super(Component.literal("Atributos"));
    }

    @Override
    protected void init() {
        super.init();
        plusButtons.clear();
        labelHits.clear();

        int x = panelX();
        int y = panelY();

        // Fecha no cliente — nao precisa de packet (a tela so existe aqui).
        addRenderableWidget(Button.builder(Component.literal("X"), b -> onClose())
                .bounds(x + PANEL_W - 28, y + 8, 18, 16)
                .tooltip(Tooltip.create(Component.literal("Fechar")))
                .build());

        prevButton = addRenderableWidget(Button.builder(Component.literal("<"), b -> changePage(-1))
                .bounds(x + 10, y + PANEL_H - 26, 28, 18)
                .tooltip(Tooltip.create(Component.literal("Página anterior")))
                .build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), b -> changePage(1))
                .bounds(x + PANEL_W - 38, y + PANEL_H - 26, 28, 18)
                .tooltip(Tooltip.create(Component.literal("Próxima página")))
                .build());

        int plusX = x + PANEL_W - 48;
        int plusY = y + 86;
        for (Attribute attribute : Attribute.values()) {
            Attribute stat = attribute;
            Button button = addRenderableWidget(Button.builder(Component.literal("+"), b ->
                            PacketDistributor.sendToServer(new DistributeAttributePayload(stat.getId())))
                    .bounds(plusX, plusY, 28, 16)
                    .tooltip(Tooltip.create(Component.literal("Gastar 1 ponto em " + stat.getDisplayName())))
                    .build());
            plusButtons.put(stat, button);
            plusY += 18;
        }

        refreshPageWidgets();
    }

    private void changePage(int delta) {
        currentPage = Math.max(0, Math.min(TOTAL_PAGES - 1, currentPage + delta));
        refreshPageWidgets();
    }

    private void refreshPageWidgets() {
        boolean onStats = currentPage == PAGE_ATTRIBUTES;
        for (Button button : plusButtons.values()) {
            button.visible = onStats;
        }
        if (prevButton != null) prevButton.active = currentPage > 0;
        if (nextButton != null) nextButton.active = currentPage < TOTAL_PAGES - 1;
        refreshPlusState();
    }

    @Override
    public void tick() {
        super.tick();
        refreshPlusState();
    }

    private void refreshPlusState() {
        AttributeData data = currentData();
        for (Attribute attribute : Attribute.values()) {
            Button button = plusButtons.get(attribute);
            if (button == null) continue;
            button.active = data.getAvailablePoints() > 0
                    && data.getLevel(attribute) < AttributeRegistry.MAX_STAT_LEVEL;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        int x = panelX();
        int y = panelY();
        graphics.fill(x, y, x + PANEL_W, y + PANEL_H, LEATHER);
        graphics.fill(x + 4, y + 4, x + PANEL_W - 4, y + PANEL_H - 4, PARCHMENT);
        graphics.fill(x + 8, y + 8, x + PANEL_W - 8, y + 28, INK);

        String title = switch (currentPage) {
            case PAGE_BONUS -> "BÔNUS DETALHADOS";
            case PAGE_GUIDE -> "EXPLICAÇÃO";
            default -> "ATRIBUTOS";
        };
        graphics.drawString(font, title, x + 16, y + 14, GOLD_LIGHT, false);
        graphics.drawString(font, (currentPage + 1) + "/" + TOTAL_PAGES,
                x + (PANEL_W / 2) - 8, y + PANEL_H - 21, MUTED, false);

        switch (currentPage) {
            case PAGE_BONUS -> renderBonusPage(graphics, x, y);
            case PAGE_GUIDE -> renderGuidePage(graphics, x, y);
            default -> renderAttributesPage(graphics, x, y);
        }

        // Não chama super.render(): Screen.render aplica o blur de novo
        // por cima do pergaminho e deixa o texto ilegível.
        for (net.minecraft.client.gui.components.Renderable widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }

        if (currentPage == PAGE_ATTRIBUTES) {
            renderAttributeTooltip(graphics, mouseX, mouseY);
        }
    }

    /**
     * Escurece o mundo sem o blur vanilla. O blur captura o HUD e
     * reaplica em cima do pergaminho, o que apagava o texto.
     */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xB0101010);
    }

    private void renderAttributesPage(GuiGraphics graphics, int x, int y) {
        AttributeData data = currentData();
        int needed = AttributeRegistry.xpToNextLevel(data.getCharacterLevel());

        graphics.drawString(font, "Nível de personagem: " + data.getCharacterLevel()
                        + " / " + AttributeRegistry.MAX_CHARACTER_LEVEL,
                x + 16, y + 36, INK, false);
        graphics.drawString(font, "Pontos disponíveis: " + data.getAvailablePoints(),
                x + 16, y + 48, data.getAvailablePoints() > 0 ? MOSS : MUTED, false);
        boolean atCap = data.getCharacterLevel() >= AttributeRegistry.MAX_CHARACTER_LEVEL;
        if (atCap) {
            graphics.drawString(font, "XP: cap atingido (nível 120)",
                    x + 16, y + 60, MUTED, false);
        } else {
            graphics.drawString(font, "XP: " + data.getCharacterXp() + " / " + needed,
                    x + 16, y + 60, MUTED, false);
        }

        int barW = 168;
        int barX = x + 16;
        int barY = y + 74;
        graphics.fill(barX, barY, barX + barW, barY + 6, INK);
        int fill = needed <= 0 ? 0 : (int) (barW * (data.getCharacterXp() / (float) needed));
        graphics.fill(barX + 1, barY + 1, barX + 1 + Math.max(0, Math.min(barW - 2, fill)), barY + 5, GOLD);

        int rowY = y + 86;
        labelHits.clear();
        for (Attribute attribute : Attribute.values()) {
            int level = data.getLevel(attribute);
            String label = AttributeMenu.rowLabel(attribute);
            graphics.drawString(font, label, x + 16, rowY + 4, INK, false);
            // Hitbox do nome para o tooltip (nao cobre o [+]).
            labelHits.put(attribute, new int[] {x + 16, rowY, 118, 16});
            fillBar(graphics, x + 138, rowY + 3, 90, 10, level, AttributeRegistry.MAX_STAT_LEVEL);
            graphics.drawString(font, String.valueOf(level), x + 234, rowY + 4, GOLD, false);
            rowY += 18;
        }
    }

    private void renderBonusPage(GuiGraphics graphics, int x, int y) {
        AttributeData data = currentData();
        int col1 = x + 16;
        int col2 = x + 186;
        int top = y + 36;

        graphics.drawString(font, "COMBATE", col1, top, OXBLOOD, false);
        drawBonus(graphics, col1, top + 12, List.of(
                String.format("Dano corpo a corpo: +%.0f%%", AttributeBonus.meleeDamageBonus(data) * 100),
                String.format("Dano à distância: +%.0f%%", AttributeBonus.rangedDamageBonus(data) * 100),
                String.format("Knockback: +%.0f%%", AttributeBonus.knockbackBonus(data) * 100),
                String.format("Esquiva: +%.0f%%", AttributeBonus.dodgeChance(data) * 100)
        ));

        graphics.drawString(font, "DEFESA", col2, top, OXBLOOD, false);
        drawBonus(graphics, col2, top + 12, List.of(
                String.format("HP máximo: +%.1f  (%s corações)",
                        AttributeBonus.extraMaxHealth(data),
                        formatHearts(AttributeBonus.extraMaxHealth(data) / 2.0)),
                String.format("Armadura: +%.1f", AttributeBonus.extraArmor(data)),
                String.format("Regeneração: +%.0f%%", AttributeBonus.regenBonus(data) * 100),
                String.format("Absorção ao comer: +%s corações",
                        formatHearts(AttributeBonus.absorptionOnEatHearts(data)))
        ));

        graphics.drawString(font, "UTILIDADE", col1, top + 78, OXBLOOD, false);
        drawBonus(graphics, col1, top + 90, List.of(
                String.format("Duração de efeitos: +%.0f%%", AttributeBonus.effectDurationBonus(data) * 100),
                String.format("Cura recebida: +%.0f%%", AttributeBonus.healBonus(data) * 100),
                String.format("XP vanilla: +%.0f%%", AttributeBonus.vanillaXpBonus(data) * 100),
                String.format("Velocidade: +%.3f", AttributeBonus.movementSpeedBonus(data)),
                String.format("Drop de minério: +%.1f%%", AttributeBonus.oreDropChance(data) * 100),
                String.format("Drop de mob: +%.1f%%", AttributeBonus.mobDropChance(data) * 100),
                String.format("Preço de villager: -%.0f%%", AttributeBonus.villagerDiscount(data) * 100)
        ));
    }

    private void renderGuidePage(GuiGraphics graphics, int x, int y) {
        int lineY = y + 36;
        for (String line : AttributeMenu.explanationLines()) {
            if (line.isEmpty()) {
                lineY += 6;
                continue;
            }
            graphics.drawString(font, line, x + 16, lineY, lineY == y + 36 ? INK : MUTED, false);
            lineY += 11;
        }
    }

    private void renderAttributeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Map.Entry<Attribute, int[]> entry : labelHits.entrySet()) {
            int[] box = entry.getValue();
            if (mouseX >= box[0] && mouseX < box[0] + box[2] && mouseY >= box[1] && mouseY < box[1] + box[3]) {
                graphics.renderTooltip(font, AttributeMenu.tooltip(entry.getKey()), java.util.Optional.empty(), mouseX, mouseY);
                return;
            }
        }
    }

    private void drawBonus(GuiGraphics graphics, int x, int y, List<String> lines) {
        for (String line : lines) {
            graphics.drawString(font, line, x, y, MUTED, false);
            y += 11;
        }
    }

    private AttributeData currentData() {
        return minecraft != null && minecraft.player != null
                ? minecraft.player.getData(ModAttachments.PLAYER_ATTRIBUTES)
                : new AttributeData();
    }

    private static String formatHearts(double hearts) {
        if (Math.abs(hearts - Math.rint(hearts)) < 0.05) return String.format("%.0f", hearts);
        return String.format("%.2f", hearts);
    }

    private static void fillBar(GuiGraphics graphics, int x, int y, int w, int h, int value, int max) {
        graphics.fill(x, y, x + w, y + h, INK);
        int fill = max <= 0 ? 0 : (int) (w * (value / (float) max));
        graphics.fill(x + 1, y + 1, x + 1 + Math.max(0, fill - 2), y + h - 1, GOLD);
    }

    private int panelX() { return (width - PANEL_W) / 2; }
    private int panelY() { return (height - PANEL_H) / 2; }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

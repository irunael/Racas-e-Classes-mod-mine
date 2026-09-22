package com.pedro.racasclasses.client.gui;

import java.util.List;

import com.pedro.racasclasses.capability.ModAttachments;
import com.pedro.racasclasses.capability.PlayerRaceData;
import com.pedro.racasclasses.network.ChooseRacePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class RaceSelectionScreen extends Screen {
    private static final int ROW_HEIGHT = 21;
    private static final int LEATHER = 0xF018100D;
    private static final int LEATHER_LIGHT = 0xFF2A1B16;
    private static final int INK = 0xFF17100D;
    private static final int PARCHMENT = 0xFFD8C39C;
    private static final int PARCHMENT_MUTED = 0xFFAA9879;
    private static final int GOLD = 0xFFC99A45;
    private static final int GOLD_LIGHT = 0xFFF0C96B;
    private static final int OXBLOOD = 0xFF642B2D;
    private static final int ARCANE = 0xFFAA82D0;
    private static final int MOSS = 0xFF86A276;
    private static final int DANGER = 0xFFD78675;

    private enum Step { RACE, SUBRACE, CLASS, SUBCLASS }

    private Step step = Step.RACE;
    private int raceIndex;
    private int subraceIndex;
    private int classIndex;
    private int subclassIndex;
    private int scrollOffset;
    private int detailScroll;
    private int detailContentHeight;
    private int detailTab;
    private boolean draggingList;
    private boolean draggingDetails;
    private boolean raceLocked;
    private int listDragOffset;
    private int detailDragOffset;
    private Button backButton;
    private Button nextButton;

    public RaceSelectionScreen() {
        super(Component.literal("Criação de personagem"));
    }

    @Override
    protected void init() {
        if (minecraft != null && minecraft.player != null) {
            PlayerRaceData saved = minecraft.player.getData(ModAttachments.PLAYER_RACE);
            if (!saved.getRaceId().equals("none")) {
                raceLocked = true;
                for (int i = 0; i < RaceOption.ALL.size(); i++) {
                    if (RaceOption.ALL.get(i).id().equals(saved.getRaceId())) {
                        raceIndex = i;
                        break;
                    }
                }
                selectSavedSubrace(saved);
                step = Step.CLASS;
            }
        }

        int y = footerButtonY();
        backButton = Button.builder(Component.literal("← Voltar"), button -> goBack())
                .bounds(panelX() + 18, y, 118, 22).build();
        nextButton = Button.builder(nextLabel(), button -> advance())
                .bounds(panelX() + panelWidth() - 218, y, 200, 22).build();
        addRenderableWidget(backButton);
        addRenderableWidget(nextButton);
        refreshButtons();
    }

    private void selectSavedSubrace(PlayerRaceData data) {
        String raceId = selectedRace().id();
        String saved = switch (raceId) {
            case "dragonborn" -> data.getDragonbornSubrace();
            case "elf" -> data.getElfSubrace();
            case "gnome" -> data.getGnomeSubrace();
            case "halfling" -> data.getHalflingSubrace();
            case "tiefling" -> data.getTieflingSubrace();
            default -> "none";
        };
        List<SubraceOption> choices = subraceChoices(selectedRace());
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).id().equals(saved)) subraceIndex = i;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int x = panelX();
        int y = panelY();
        int w = panelWidth();
        int h = panelHeight();

        // Livro de couro, filetes dourados e cantos reforçados.
        graphics.fill(x - 3, y - 3, x + w + 3, y + h + 3, 0xD9080605);
        graphics.fill(x, y, x + w, y + h, LEATHER);
        graphics.fill(x + 4, y + 4, x + w - 4, y + h - 4, 0xFF241713);
        drawFrame(graphics, x + 8, y + 8, x + w - 8, y + h - 8, GOLD);
        drawCornerOrnaments(graphics, x + 9, y + 9, w - 18, h - 18);

        graphics.drawString(font, "Crônica do aventureiro", x + 22, y + 15, GOLD_LIGHT, false);
        graphics.drawString(font, stepSubtitle(), x + 22, y + 28, PARCHMENT_MUTED, false);
        renderProgress(graphics, x + 22, y + 43, w - 44);

        int listX = x + 18;
        int listTop = contentTop();
        int listBottom = contentBottom();
        int listW = listWidth();
        graphics.fill(listX - 3, listTop - 3, listX + listW + 8, listBottom + 3, 0xB50C0807);
        drawFrame(graphics, listX - 3, listTop - 3, listX + listW + 8, listBottom + 3, 0xFF76562E);
        renderList(graphics, mouseX, mouseY, listX, listTop, listW, listBottom);

        int detailX = detailX();
        int detailTop = listTop;
        int detailBottom = listBottom;
        int detailW = detailWidth();
        graphics.fill(detailX - 6, detailTop - 3, detailX + detailW + 9, detailBottom + 3, 0xFF33261C);
        graphics.fill(detailX - 3, detailTop, detailX + detailW + 6, detailBottom, 0xFF18110E);
        renderDetailHeader(graphics, detailX, detailTop, detailW);
        graphics.enableScissor(detailX, detailBodyTop(), detailX + detailW, detailBottom);
        detailContentHeight = renderDetails(graphics, detailX, detailBodyTop() - detailScroll, detailW);
        graphics.disableScissor();
        detailScroll = Mth.clamp(detailScroll, 0, detailMaxScroll());
        renderScrollbar(graphics, detailX + detailW + 2, detailBodyTop(), detailBottom, true);

        graphics.fill(x + 18, contentBottom() + 9, x + w - 18, contentBottom() + 10, 0xFF513923);
        backButton.render(graphics, mouseX, mouseY, partialTick);
        nextButton.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderProgress(GuiGraphics graphics, int x, int y, int width) {
        String[] names = {"Raça", "Linhagem", "Classe", "Arquétipo"};
        int current = step.ordinal();
        int cursor = x;
        graphics.fill(x, y + 13, x + width, y + 14, 0xFF4C3827);
        for (int i = 0; i < 4; i++) {
            boolean done = i < current;
            boolean active = i == current;
            String label = (done ? "✓ " : (i + 1) + " ") + names[i];
            int color = active ? GOLD_LIGHT : done ? MOSS : PARCHMENT_MUTED;
            if (active) graphics.fill(cursor - 4, y - 2, cursor + font.width(label) + 4, y + 11, INK);
            graphics.drawString(font, label, cursor, y, color, false);
            cursor += font.width(label) + 13;
            if (i < 3) {
                graphics.drawString(font, "›", cursor - 7, y, 0xFF70583D, false);
            }
        }
    }

    private void renderList(GuiGraphics graphics, int mouseX, int mouseY, int x, int top, int width, int bottom) {
        int visible = visibleRows();
        int end = Math.min(entryCount(), scrollOffset + visible + 1);
        graphics.enableScissor(x, top, x + width, bottom);
        for (int i = scrollOffset; i < end; i++) {
            int rowY = top + (i - scrollOffset) * ROW_HEIGHT;
            boolean selected = i == selectedIndex();
            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2;
            graphics.fill(x, rowY, x + width, rowY + ROW_HEIGHT - 2,
                    selected ? OXBLOOD : hovered ? 0xFF493125 : LEATHER_LIGHT);
            if (selected) {
                graphics.fill(x, rowY, x + 3, rowY + ROW_HEIGHT - 2, GOLD_LIGHT);
                graphics.fill(x + width - 3, rowY, x + width, rowY + ROW_HEIGHT - 2, GOLD);
            }
            graphics.drawString(font, entryName(i), x + 10, rowY + 6,
                    selected ? GOLD_LIGHT : PARCHMENT, false);
            if (entryHasChildren(i)) graphics.drawString(font, "✦", x + width - 14, rowY + 6, ARCANE, false);
        }
        graphics.disableScissor();
        renderScrollbar(graphics, x + width + 3, top, bottom, false);
    }

    private int renderDetails(GuiGraphics graphics, int x, int y, int width) {
        return switch (step) {
            case RACE -> renderRaceDetails(graphics, x, y, width);
            case SUBRACE -> renderSubraceDetails(graphics, x, y, width);
            case CLASS -> renderClassDetails(graphics, x, y, width);
            case SUBCLASS -> renderSubclassDetails(graphics, x, y, width);
        };
    }

    private void renderDetailHeader(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, detailTitle(), x, y + 6, GOLD_LIGHT, false);
        graphics.drawString(font, detailSubtitle(), x, y + 18, PARCHMENT_MUTED, false);
        String[] tabs = detailTabs();
        int tabY = detailTabsY();
        int gap = 3;
        int tabWidth = (width - gap * (tabs.length - 1)) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            int tabX = x + i * (tabWidth + gap);
            int tabRight = i == tabs.length - 1 ? x + width : tabX + tabWidth;
            boolean active = i == detailTab;
            graphics.fill(tabX, tabY, tabRight, tabY + 17, active ? 0xFF493321 : 0xFF261914);
            graphics.fill(tabX, tabY + 16, tabRight, tabY + 17, active ? GOLD : 0xFF4C3827);
            graphics.drawCenteredString(font, tabs[i], (tabX + tabRight) / 2, tabY + 4,
                    active ? GOLD_LIGHT : PARCHMENT_MUTED);
        }
    }

    private String detailTitle() {
        return switch (step) {
            case RACE -> selectedRace().name();
            case SUBRACE -> subraceChoices(selectedRace()).get(subraceIndex).name();
            case CLASS -> selectedClass().name();
            case SUBCLASS -> selectedSubclass().name();
        };
    }

    private String detailSubtitle() {
        return switch (step) {
            case RACE -> "Origem e legado";
            case SUBRACE -> "Linhagem de " + selectedRace().name();
            case CLASS -> "Vocação";
            case SUBCLASS -> "Arquétipo de " + selectedClass().name();
        };
    }

    private String[] detailTabs() {
        return switch (step) {
            case RACE -> new String[] {"Resumo", "Atributos", "Habilidades"};
            case SUBRACE -> selectedSubrace().powers().isEmpty()
                    ? new String[] {"Resumo", "Atributos", "Fraquezas"}
                    : new String[] {"Resumo", "Atributos", "Poderes", "Fraquezas"};
            case CLASS -> new String[] {"Resumo", "Características", "Arquétipos"};
            case SUBCLASS -> new String[] {"Resumo", "Poderes"};
        };
    }

    private int renderRaceDetails(GuiGraphics graphics, int x, int y, int width) {
        RaceOption option = selectedRace();
        int start = y;
        int cursor = y + 4;
        if (detailTab == 0) {
            cursor = paragraph(graphics, option.summary(), x, cursor, width, PARCHMENT) + 14;
            graphics.drawString(font, "Linhagem", x, cursor, ARCANE, false);
            cursor += 15;
            String lineage = option.subraces().isEmpty()
                    ? "Esta raça não possui linhagens adicionais. Essa etapa será ignorada."
                    : option.subraces().size() + " linhagens disponíveis para escolher na próxima etapa.";
            cursor = paragraph(graphics, lineage, x, cursor, width, PARCHMENT_MUTED);
        } else if (detailTab == 1) {
            cursor = section(graphics, "Atributos", option.attributes(), x, cursor, width, MOSS);
            if (!option.weaknesses().isEmpty())
                cursor = listSection(graphics, "Fraquezas", option.weaknesses(), x, cursor + 7, width, DANGER);
        } else {
            cursor = listSection(graphics, "Habilidades raciais", option.abilities(), x, cursor, width, MOSS);
        }
        return cursor - start + 12;
    }

    private int renderSubraceDetails(GuiGraphics graphics, int x, int y, int width) {
        SubraceOption option = selectedSubrace();
        int start = y;
        int cursor = y + 4;
        if (detailTab == 0) {
            cursor = paragraph(graphics, option.summary(), x, cursor, width, PARCHMENT) + 14;
            cursor = paragraph(graphics, "Confira os atributos, benefícios e limitações desta linhagem nas abas acima. Os traços herdados da raça também estão incluídos.",
                    x, cursor, width, PARCHMENT_MUTED) + 12;
            cursor = paragraph(graphics, "A linhagem é escolhida por você, sem sorteio.", x, cursor, width, PARCHMENT_MUTED);
        } else if (detailTab == 1) {
            cursor = section(graphics, "Atributos", option.attributes(), x, cursor, width, MOSS);
            if (!option.buffs().isEmpty())
                cursor = listSection(graphics, "Buffs", option.buffs(), x, cursor + 7, width, MOSS) + 8;
            if (!option.debuffs().isEmpty())
                cursor = listSection(graphics, "Debuffs", option.debuffs(), x, cursor + 7, width, DANGER);
        } else if (detailTab == 2 && !option.powers().isEmpty()) {
            cursor = listSection(graphics, "Poderes", option.powers(), x, cursor, width, ARCANE);
        } else {
            if (!option.debuffs().isEmpty())
                cursor = listSection(graphics, "Fraquezas", option.debuffs(), x, cursor, width, DANGER);
            else
                cursor = paragraph(graphics, "Esta linhagem não possui fraquezas específicas além das herdadas da raça base.", x, cursor, width, PARCHMENT_MUTED);
        }
        return cursor - start + 8;
    }

    private int renderClassDetails(GuiGraphics graphics, int x, int y, int width) {
        ClassOption option = selectedClass();
        int start = y;
        int cursor = y + 4;
        if (detailTab == 0) {
            cursor = paragraph(graphics, option.summary(), x, cursor, width, PARCHMENT);
        } else if (detailTab == 1) {
            cursor = listSection(graphics, "Características", option.abilities(), x, cursor, width, MOSS);
        } else {
            graphics.drawString(font, "Arquétipos", x, cursor, ARCANE, false);
            cursor += 15;
            if (option.subclasses().isEmpty()) {
                cursor = paragraph(graphics, "Esta classe não possui arquétipos. A etapa será ignorada.",
                        x, cursor, width, PARCHMENT_MUTED);
            } else {
                for (ClassOption.SubclassOption subclass : option.subclasses()) {
                    cursor = paragraph(graphics, "• " + subclass.name(), x, cursor, width, PARCHMENT) + 4;
                }
                cursor = paragraph(graphics, "A escolha acontece na próxima etapa.", x, cursor + 6, width, PARCHMENT_MUTED);
            }
        }
        return cursor - start + 8;
    }

    private int renderSubclassDetails(GuiGraphics graphics, int x, int y, int width) {
        ClassOption.SubclassOption option = selectedSubclass();
        int start = y;
        int cursor = y + 4;
        if (detailTab == 0) {
            cursor = paragraph(graphics, option.summary(), x, cursor, width, PARCHMENT) + 14;
            graphics.drawString(font, "Pronto para iniciar", x, cursor, GOLD_LIGHT, false);
            cursor += 15;
            cursor = paragraph(graphics, "Concluir grava raça, linhagem, classe e arquétipo neste mundo.",
                    x, cursor, width, PARCHMENT_MUTED);
        } else {
            cursor = listSection(graphics, "Poderes", option.abilities(), x, cursor, width, ARCANE);
        }
        return cursor - start + 8;
    }

    private int section(GuiGraphics g, String title, String body, int x, int y, int width, int color) {
        g.drawString(font, title, x, y, color, false);
        return paragraph(g, body, x, y + 14, width, PARCHMENT_MUTED) + 8;
    }

    private int listSection(GuiGraphics g, String title, List<String> entries, int x, int y, int width, int color) {
        g.drawString(font, title, x, y, color, false);
        int cursor = y + 14;
        for (String entry : entries) cursor = paragraph(g, "• " + entry, x, cursor, width, PARCHMENT) + 3;
        return cursor;
    }

    private int paragraph(GuiGraphics graphics, String text, int x, int y, int width, int color) {
        int cursor = y;
        for (var line : font.split(Component.literal(text), width)) {
            graphics.drawString(font, line, x, cursor, color, false);
            cursor += 11;
        }
        return cursor;
    }

    private void advance() {
        switch (step) {
            case RACE -> step = selectedRace().subraces().isEmpty() ? Step.CLASS : Step.SUBRACE;
            case SUBRACE -> step = Step.CLASS;
            case CLASS -> {
                if (selectedClass().subclasses().isEmpty()) finishCreation();
                else step = Step.SUBCLASS;
            }
            case SUBCLASS -> finishCreation();
        }
        resetView();
    }

    private void goBack() {
        if (raceLocked && step == Step.CLASS) return;
        step = switch (step) {
            case RACE -> Step.RACE;
            case SUBRACE -> Step.RACE;
            case CLASS -> selectedRace().subraces().isEmpty() ? Step.RACE : Step.SUBRACE;
            case SUBCLASS -> Step.CLASS;
        };
        resetView();
    }

    private void finishCreation() {
        String subrace = selectedRace().subraces().isEmpty() ? "none" : subraceChoices(selectedRace()).get(subraceIndex).id();
        String subclass = selectedClass().subclasses().isEmpty() ? "none" : selectedSubclass().id();
        PacketDistributor.sendToServer(new ChooseRacePayload(selectedRace().id(), subrace, selectedClass().id(), subclass));
        if (minecraft != null) minecraft.setScreen(null);
    }

    private void resetView() {
        scrollOffset = 0;
        detailScroll = 0;
        detailTab = 0;
        draggingList = false;
        draggingDetails = false;
        refreshButtons();
    }

    private void refreshButtons() {
        if (backButton == null || nextButton == null) return;
        backButton.active = step != Step.RACE && !(raceLocked && step == Step.CLASS);
        nextButton.setMessage(nextLabel());
    }

    private Component nextLabel() {
        return Component.literal(step == Step.SUBCLASS || (step == Step.CLASS && selectedClass().subclasses().isEmpty())
                ? "Concluir jornada" : "Continuar →");
    }

    private String stepSubtitle() {
        return switch (step) {
            case RACE -> "Escolha o povo que moldou sua origem.";
            case SUBRACE -> "Defina a linhagem que corre em seu sangue.";
            case CLASS -> "Escolha como seu aventureiro enfrenta o mundo.";
            case SUBCLASS -> "Escolha o caminho que especializa seus talentos.";
        };
    }

    private int entryCount() {
        return switch (step) {
            case RACE -> RaceOption.ALL.size();
            case SUBRACE -> subraceChoices(selectedRace()).size();
            case CLASS -> ClassOption.ALL.size();
            case SUBCLASS -> selectedClass().subclasses().size();
        };
    }

    private int selectedIndex() {
        return switch (step) {
            case RACE -> raceIndex;
            case SUBRACE -> subraceIndex;
            case CLASS -> classIndex;
            case SUBCLASS -> subclassIndex;
        };
    }

    private void setSelectedIndex(int value) {
        switch (step) {
            case RACE -> { raceIndex = value; subraceIndex = 0; }
            case SUBRACE -> subraceIndex = value;
            case CLASS -> { classIndex = value; subclassIndex = 0; }
            case SUBCLASS -> subclassIndex = value;
        }
        detailTab = 0;
        detailScroll = 0;
    }

    private String entryName(int index) {
        return switch (step) {
            case RACE -> RaceOption.ALL.get(index).name();
            case SUBRACE -> subraceChoices(selectedRace()).get(index).name();
            case CLASS -> ClassOption.ALL.get(index).name();
            case SUBCLASS -> selectedClass().subclasses().get(index).name();
        };
    }

    private boolean entryHasChildren(int index) {
        return switch (step) {
            case RACE -> !RaceOption.ALL.get(index).subraces().isEmpty();
            case CLASS -> !ClassOption.ALL.get(index).subclasses().isEmpty();
            default -> false;
        };
    }

    private RaceOption selectedRace() { return RaceOption.ALL.get(raceIndex); }
    private SubraceOption selectedSubrace() { return subraceChoices(selectedRace()).get(subraceIndex); }
    private ClassOption selectedClass() { return ClassOption.ALL.get(classIndex); }
    private ClassOption.SubclassOption selectedSubclass() { return selectedClass().subclasses().get(subclassIndex); }

    private List<SubraceOption> subraceChoices(RaceOption race) {
        return SubraceOption.forRace(race.id());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && handleDetailTabClick(mouseX, mouseY)) return true;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && overScrollbar(mouseX, mouseY, true)) {
            handleScrollbar(mouseY, true); return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && overScrollbar(mouseX, mouseY, false)) {
            handleScrollbar(mouseY, false); return true;
        }
        int x = panelX() + 18;
        int top = contentTop();
        int bottom = contentBottom();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseX >= x && mouseX < x + listWidth()
                && mouseY >= top && mouseY < bottom) {
            int index = scrollOffset + (int) ((mouseY - top) / ROW_HEIGHT);
            if (index >= 0 && index < entryCount()) {
                setSelectedIndex(index);
                keepSelectionVisible();
                refreshButtons();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleDetailTabClick(double mouseX, double mouseY) {
        String[] tabs = detailTabs();
        int x = detailX();
        int width = detailWidth();
        int y = detailTabsY();
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + 17) return false;
        int gap = 3;
        int tabWidth = (width - gap * (tabs.length - 1)) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            int left = x + i * (tabWidth + gap);
            int right = i == tabs.length - 1 ? x + width : left + tabWidth;
            if (mouseX >= left && mouseX < right) {
                detailTab = i;
                detailScroll = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (overDetails(mouseX, mouseY)) {
            detailScroll = Mth.clamp(detailScroll - (int) Math.signum(scrollY) * 25, 0, detailMaxScroll());
        } else {
            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(scrollY), 0, maxScrollOffset());
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingDetails) { updateFromThumb(mouseY - detailDragOffset, true); return true; }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingList) { updateFromThumb(mouseY - listDragOffset, false); return true; }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingList = draggingDetails = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderScrollbar(GuiGraphics g, int x, int top, int bottom, boolean details) {
        int max = details ? detailMaxScroll() : maxScrollOffset();
        if (max <= 0) return;
        int trackTop = top + 3;
        int trackHeight = bottom - top - 6;
        int thumbH = thumbHeight(trackHeight, details);
        int thumbY = thumbY(trackTop, trackHeight, thumbH, details);
        g.fill(x, top, x + 7, bottom, 0xFF0D0907);
        g.fill(x + 2, trackTop, x + 5, top + trackHeight + 3, 0xFF5A432D);
        g.fill(x + 1, thumbY, x + 6, thumbY + thumbH, GOLD);
    }

    private boolean overScrollbar(double mouseX, double mouseY, boolean details) {
        int listX = panelX() + 18;
        int top = details ? detailBodyTop() : contentTop();
        int bottom = contentBottom();
        int x = details ? detailX() + detailWidth() + 2 : listX + listWidth() + 3;
        int max = details ? detailMaxScroll() : maxScrollOffset();
        return max > 0 && mouseX >= x && mouseX < x + 7 && mouseY >= top && mouseY < bottom;
    }

    private void handleScrollbar(double mouseY, boolean details) {
        int top = (details ? detailBodyTop() : contentTop()) + 3;
        int trackHeight = contentBottom() - (details ? detailBodyTop() : contentTop()) - 6;
        int thumbH = thumbHeight(trackHeight, details);
        int thumbY = thumbY(top, trackHeight, thumbH, details);
        if (mouseY >= thumbY && mouseY < thumbY + thumbH) {
            if (details) { draggingDetails = true; detailDragOffset = (int) mouseY - thumbY; }
            else { draggingList = true; listDragOffset = (int) mouseY - thumbY; }
        } else updateFromThumb(mouseY - thumbH / 2.0, details);
    }

    private void updateFromThumb(double proposedY, boolean details) {
        int top = (details ? detailBodyTop() : contentTop()) + 3;
        int trackHeight = contentBottom() - (details ? detailBodyTop() : contentTop()) - 6;
        int thumbH = thumbHeight(trackHeight, details);
        int travel = Math.max(1, trackHeight - thumbH);
        double ratio = Mth.clamp((proposedY - top) / travel, 0.0, 1.0);
        if (details) detailScroll = (int) Math.round(ratio * detailMaxScroll());
        else scrollOffset = (int) Math.round(ratio * maxScrollOffset());
    }

    private int thumbHeight(int trackHeight, boolean details) {
        if (details) return Math.min(trackHeight, Math.max(24, trackHeight * detailViewportHeight() / Math.max(1, detailContentHeight)));
        return Math.min(trackHeight, Math.max(24, trackHeight * visibleRows() / Math.max(1, entryCount())));
    }

    private int thumbY(int top, int trackHeight, int thumbH, boolean details) {
        int max = details ? detailMaxScroll() : maxScrollOffset();
        int value = details ? detailScroll : scrollOffset;
        return top + (max == 0 ? 0 : (trackHeight - thumbH) * value / max);
    }

    private boolean overDetails(double mouseX, double mouseY) {
        int left = detailX();
        return mouseX >= left && mouseX < panelX() + panelWidth() - 20
                && mouseY >= contentTop() && mouseY < contentBottom();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            int delta = keyCode == GLFW.GLFW_KEY_UP ? -1 : 1;
            setSelectedIndex(Mth.clamp(selectedIndex() + delta, 0, entryCount() - 1));
            keepSelectionVisible();
            refreshButtons();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
            int delta = keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1;
            detailTab = Mth.clamp(detailTab + delta, 0, detailTabs().length - 1);
            detailScroll = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { advance(); return true; }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && step != Step.RACE && !(raceLocked && step == Step.CLASS)) {
            goBack(); return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void keepSelectionVisible() {
        if (selectedIndex() < scrollOffset) scrollOffset = selectedIndex();
        if (selectedIndex() >= scrollOffset + visibleRows()) scrollOffset = selectedIndex() - visibleRows() + 1;
    }

    private void drawFrame(GuiGraphics g, int left, int top, int right, int bottom, int color) {
        g.fill(left, top, right, top + 1, color);
        g.fill(left, bottom - 1, right, bottom, color);
        g.fill(left, top, left + 1, bottom, color);
        g.fill(right - 1, top, right, bottom, color);
    }

    private void drawCornerOrnaments(GuiGraphics g, int x, int y, int w, int h) {
        int c = GOLD_LIGHT;
        g.fill(x, y, x + 13, y + 2, c); g.fill(x, y, x + 2, y + 13, c);
        g.fill(x + w - 13, y, x + w, y + 2, c); g.fill(x + w - 2, y, x + w, y + 13, c);
        g.fill(x, y + h - 2, x + 13, y + h, c); g.fill(x, y + h - 13, x + 2, y + h, c);
        g.fill(x + w - 13, y + h - 2, x + w, y + h, c); g.fill(x + w - 2, y + h - 13, x + w, y + h, c);
    }

    private int contentTop() { return panelY() + 64; }
    private int contentBottom() { return panelY() + panelHeight() - 47; }
    private int footerButtonY() { return panelY() + panelHeight() - 35; }
    private int detailX() { return panelX() + 18 + listWidth() + 22; }
    private int detailWidth() { return panelX() + panelWidth() - 27 - detailX(); }
    private int detailTabsY() { return contentTop() + 31; }
    private int detailBodyTop() { return contentTop() + 54; }
    private int visibleRows() { return Math.max(1, (contentBottom() - contentTop()) / ROW_HEIGHT); }
    private int maxScrollOffset() { return Math.max(0, entryCount() - visibleRows()); }
    private int detailViewportHeight() { return Math.max(1, contentBottom() - detailBodyTop()); }
    private int detailMaxScroll() { return Math.max(0, detailContentHeight - detailViewportHeight()); }
    private int panelWidth() { return Math.min(width - 18, 980); }
    private int panelHeight() { return Math.min(height - 18, 560); }
    private int panelX() { return (width - panelWidth()) / 2; }
    private int panelY() { return (height - panelHeight()) / 2; }
    private int listWidth() { return Mth.clamp(panelWidth() / 5, 155, 195); }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean isPauseScreen() { return true; }
}

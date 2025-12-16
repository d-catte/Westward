package io.github.onu_eccs1621_sp2025.westward.screen;

import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiInputTextCallbackData;
import imgui.ImVec2;
import imgui.callback.ImGuiInputTextCallback;
import imgui.flag.*;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.data.Date;
import io.github.onu_eccs1621_sp2025.westward.data.SaveData;
import io.github.onu_eccs1621_sp2025.westward.data.member.Gender;
import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.data.member.Role;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.screen.default_screens.accident.ConsequenceScreen;
import io.github.onu_eccs1621_sp2025.westward.utils.Config;
import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;
import io.github.onu_eccs1621_sp2025.westward.utils.WebUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.MemberPlaqueData;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Renders all UI elements for the game
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.1
 */
public class Renderer {
    private MainMenuScreen currentMainMenuScreen = MainMenuScreen.MENU;
    private GameStage currentStage = GameStage.MAIN_MENU;
    /**
     * The queue for all screens
     */
    public static final ConcurrentLinkedDeque<String> RENDER_QUEUE = new ConcurrentLinkedDeque<>();
    private Screen currentScreen = null;
    private CursorType cursorType = CursorType.WAGON;
    /**
     * The game's FPS limit
     */
    private static final int[] FPS = { Config.getConfig().getFpsLimit() };

    // Data for a new game
    private final ImString saveName = new ImString();
    private final ImInt difficulty = new ImInt(1);
    private final ImString money = new ImString("$", 8);
    private final ImInt month = new ImInt(0);
    private static final String[] MONTHS = Date.Month.getMonths();
    private static final String[] FEMALE_ROLES = Role.getRoles(Gender.FEMALE);
    private static final String[] MALE_ROLES = Role.getRoles(Gender.MALE);
    private final ImString day = new ImString();
    private static final List<MemberPlaqueData> MEMBER_DATA = new ArrayList<>(Config.getConfig().getTeamMemberCount());


    // Input constraints
    private final ImGuiInputTextCallback inputDayMask = new ImGuiInputTextCallback() {
        @Override
        public void accept(ImGuiInputTextCallbackData data) {
            // Ensure only integers are typed
            if (data.getBufTextLen() > 0) {
                int pos = data.getBufTextLen() - 1;
                char eventChar = data.getBuf().charAt(pos);
                if (eventChar < '0' || eventChar > '9' || data.getBufTextLen() > 2) {
                    data.deleteChars(pos, 1);
                }
            }
        }
    };
    private final ImGuiInputTextCallback inputMoneyMask = new ImGuiInputTextCallback() {
        @Override
        public void accept(ImGuiInputTextCallbackData data) {
            String buffer = data.getBuf();
            int length = data.getBufTextLen();

            // Disallow deleting the dollar sign
            if (length == 0 || buffer.charAt(0) != '$') {
                data.insertChars(0, "$");
                return;
            }

            int decimalIndex = buffer.indexOf('.');
            boolean hasDecimal = decimalIndex != -1;
            int decimalCount = hasDecimal ? length - decimalIndex - 1 : 0;

            if (length > 1) {
                int pos = length - 1;
                char eventChar = buffer.charAt(pos);

                // Remove invalid characters
                if ((eventChar < '0' || eventChar > '9') && eventChar != '.') {
                    data.deleteChars(pos, 1);
                } else if (eventChar == '.' && hasDecimal && decimalIndex != pos) {
                    data.deleteChars(pos, 1);
                } else if (hasDecimal && decimalCount > 2) {
                    // Limit to two decimal places
                    data.deleteChars(pos, 1);
                }
            }
        }
    };

    /**
     * Gets the current FPS limiter
     * @return Current FPS limiter
     */
    public int getFPS() {
        return FPS[0];
    }

    /**
     * Renders all UI elements in the entire game
     */
    public void render() {
        ImGui.setNextWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
        ImGui.setNextWindowPos(0, 0);
        ImGui.begin("Westward", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse);
        switch (this.currentStage) {
            case MAIN_MENU -> renderMainMenu();
            case GAME -> {
                if (this.currentScreen != null) {
                    this.currentScreen.render();
                    if (!this.currentScreen.isVisible()) {
                        this.currentScreen = null;
                    }
                } else if (!RENDER_QUEUE.isEmpty()) {
                    // Render screens in the render queue
                    String id = RENDER_QUEUE.peek();
                    if (Registry.containsAsset(Registry.AssetType.SCREEN, id)) {
                        this.currentScreen = (Screen) Registry.getAsset(Registry.AssetType.SCREEN, id);
                    } else if (Registry.containsAsset(Registry.AssetType.HUNTING_SCREEN, id)) {
                        this.currentScreen = (Screen) Registry.getAsset(Registry.AssetType.HUNTING_SCREEN, id);
                    }
                    this.currentScreen.setVisible();
                    DebugLogger.info("CurrentScreen: {}", this.currentScreen.getId());
                } else if (Game.getInstance() != null) {
                    Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
                    Dashboard.render();
                    Format.clearFontSize();
                }
            }
        }
        ImGui.end();
    }

    private void renderMainMenu() {
        switch (this.currentMainMenuScreen) {
            case MENU -> renderMenu();
            case SETTINGS -> renderSettings();
            case NEW_GAME -> renderCreateGame();
            case LOAD_SAVE -> renderSaves();
            case TRAIL_SELECTION -> renderTrailSelection();
            case MEMBER_SELECTION -> renderMemberSelection();
            case LANG_SELECTION -> renderLangSelection();
        }
    }

    private void renderMenu() {
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImVec2 backgroundSize = RenderUtils.getItemScaleMaintainAspectRatio(1.225F, 0.98F);
        RenderUtils.centeredHorizontal(backgroundSize.x, RenderUtils.getCursorRelative(0F, 0.01F).y);
        ImGui.image((Long) Registry.getAsset(Registry.AssetType.ASSET, "courthouseAndJailRocks"), backgroundSize);

        RenderUtils.centeredHorizontal(ImGui.calcTextSize("Westward").x, 20);
        ImGui.text("Westward");

        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);

        ImVec2 menuButtonSize = RenderUtils.getCursorRelative(0.4F, 0.1875F);
        ImVec2 newGameButtonPos = RenderUtils.getCursorRelative(0, 0.125F);
        RenderUtils.centeredHorizontal(menuButtonSize.x, newGameButtonPos.y);
        ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
        if (ImGui.imageButton("createNewGameButton", (Long) Registry.getAsset(Registry.AssetType.ASSET, "button"), menuButtonSize.x, menuButtonSize.y)) {
            this.currentMainMenuScreen = MainMenuScreen.NEW_GAME;
            SoundEngine.loadSFX(Audio.CLICK_1);
        }
        ImVec2 newGameLabelPos = RenderUtils.getCursorRelative(0, 0.1875F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.main.createNewGame")).x, newGameLabelPos.y);
        ImGui.textColored(0, 0, 0, 255, Translations.getTranslatedText("menu.main.createNewGame"));

        ImVec2 loadSaveButtonPos = RenderUtils.getCursorRelative(0, 0.375F);
        RenderUtils.centeredHorizontal(menuButtonSize.x, loadSaveButtonPos.y);
        if (ImGui.imageButton("loadSaveButton", (Long) Registry.getAsset(Registry.AssetType.ASSET, "button"), menuButtonSize.x, menuButtonSize.y)) {
            this.currentMainMenuScreen = MainMenuScreen.LOAD_SAVE;
            SoundEngine.loadSFX(Audio.CLICK_1);
        }
        ImVec2 loadSaveLabelPos = RenderUtils.getCursorRelative(0, 0.4375F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.main.loadSave")).x, loadSaveLabelPos.y);
        ImGui.textColored(0, 0, 0, 255, Translations.getTranslatedText("menu.main.loadSave"));

        ImVec2 settingsButtonPos = RenderUtils.getCursorRelative(0, 0.625F);
        RenderUtils.centeredHorizontal(menuButtonSize.x, settingsButtonPos.y);
        if (ImGui.imageButton("settingsButton", (Long) Registry.getAsset(Registry.AssetType.ASSET, "button"), menuButtonSize)) {
            this.currentMainMenuScreen = MainMenuScreen.SETTINGS;
            SoundEngine.loadSFX(Audio.CLICK_1);
        }
        ImVec2 settingsButtonLabel = RenderUtils.getCursorRelative(0, 0.6875F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.main.settings")).x, settingsButtonLabel.y);
        ImGui.textColored(0, 0, 0, 255, Translations.getTranslatedText("menu.main.settings"));

        ImVec2 langButtonPos = RenderUtils.getCursorRelative(0.006F, 0.905F);
        ImVec2 langButtonSize = RenderUtils.getItemScaleMaintainAspectRatio(0.1F, 0.078125F);
        ImGui.setCursorPos(langButtonPos);
        if (ImGui.imageButton("langButton", (Long) Registry.getAsset(Registry.AssetType.ASSET, "lang"), langButtonSize)) {
            this.currentMainMenuScreen = MainMenuScreen.LANG_SELECTION;
            SoundEngine.loadSFX(Audio.CLICK_1);
        }

        ImGui.popStyleColor();
        Format.clearFontSize();
    }

    private void renderSettings() {
        // Render the settings menu
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImVec2 settingsTitlePos = RenderUtils.getCursorRelative(0, 0.025F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.main.settings")).x, settingsTitlePos.y);
        ImGui.text(Translations.getTranslatedText("menu.main.settings"));
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);

        // Music Volume Controls
        volumeSlider(true);

        // SFX Volume Controls
        volumeSlider(false);

        // FPS Controls
        fpsSlider();

        // Wiki Button
        ImVec2 wikiButtonSize = RenderUtils.getCursorRelative(0.25F, 0.1F);
        ImVec2 wikiButtonPos = RenderUtils.getCursorRelative(0.1F, 0.6F);
        ImGui.setCursorPos(wikiButtonPos);
        if (ImGui.button(Translations.getTranslatedText("menu.main.wiki"), wikiButtonSize)) {
            WebUtils.openWebPage("https://github.com/d-catte/Westward/wiki");
        }

        if (this.renderHomeButton()) {
            Config.getConfig().saveConfig();
        }
        Format.clearFontSize();
    }

    private void volumeSlider(boolean music) {
        int mult = music ? 1 : 2;
        ImVec2 volumeSliderPos = RenderUtils.getCursorRelative(0.1F, 0.15F * mult);
        ImVec2 volumeSliderSize = RenderUtils.getCursorRelative(0.25F, 0.0F);
        ImGui.setCursorPos(volumeSliderPos);
        ImGui.setNextItemWidth(volumeSliderSize.x);
        String translation = music ? "menu.main.volume" : "menu.main.sfxVolume";
        if (music) {
            if (ImGui.sliderInt(Translations.getTranslatedText(translation), SoundEngine.MUSIC_VOLUME, 0, 100)) {
                SoundEngine.updateMusicVolume();
            }
        } else if (ImGui.sliderInt(Translations.getTranslatedText(translation), SoundEngine.SFX_VOLUME, 0, 100)) {
            SoundEngine.updateSfxVolume();
        }

        // Volume Slider released
        if (ImGui.isItemDeactivatedAfterEdit()) {
            Config.getConfig().setMusicVolume(SoundEngine.MUSIC_VOLUME[0]);
            Config.getConfig().setSfxVolume(SoundEngine.SFX_VOLUME[0]);
            SoundEngine.loadSFX(Audio.CLICK_2);
        }
    }

    private void fpsSlider() {
        ImVec2 fpsSliderPos = RenderUtils.getCursorRelative(0.1F, 0.45F);
        ImVec2 fpsSliderSize = RenderUtils.getCursorRelative(0.25F, 0.0F);
        ImGui.setCursorPos(fpsSliderPos);
        ImGui.setNextItemWidth(fpsSliderSize.x);
        ImGui.sliderInt(Translations.getTranslatedText("menu.main.fps"), FPS, 5, 144, FPS[0] == 144 ? "Unlimited" : "%d");

        if (ImGui.isItemDeactivatedAfterEdit()) {
            if (FPS[0] == 144) {
                // Set to unlimited
                TrailApplication.setFPSLimit(-1);
                Config.getConfig().setFpsLimit(-1);
            } else {
                TrailApplication.setFPSLimit(FPS[0]);
                Config.getConfig().setFpsLimit(FPS[0]);
            }
        }
    }

    private void renderCreateGame() {
        // Render the new game menu
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImVec2 newGameTitlePos = RenderUtils.getCursorRelative(0, 0.025F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.newGame.newGame")).x, newGameTitlePos.y);
        ImGui.text(Translations.getTranslatedText("menu.newGame.newGame"));
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        // Text
        ImVec2 saveTextPos = RenderUtils.getCursorRelative(0.0F, 0.2F);
        String saveNameText = Translations.getTranslatedText("menu.newGame.saveName");
        float textWidth = ImGui.calcTextSizeX(saveNameText);
        ImGui.setCursorPos(saveTextPos);
        RenderUtils.centeredHorizontal(textWidth, saveTextPos.y);
        ImGui.text(saveNameText);

        // Input
        ImVec2 inputTextWidth = RenderUtils.getCursorRelative(0.7F, 0.0F);
        ImVec2 inputTextPos = RenderUtils.getCursorRelative(0.0F,0.25F);
        RenderUtils.centeredHorizontal(inputTextWidth.x, inputTextPos.y);
        ImGui.setNextItemWidth(inputTextWidth.x);
        ImGui.inputText("##saveName", this.saveName);
        ImVec2 createButtonPos = RenderUtils.getCursorRelative(0, 0.35F);
        ImVec2 createButtonSize = RenderUtils.getCursorRelative(0.2F, 0.094F);
        RenderUtils.centeredHorizontal(createButtonSize.x, createButtonPos.y);
        if (ImGui.button(Translations.getTranslatedText("menu.newGame.create"), createButtonSize.x, createButtonSize.y)) {
            if (!this.saveName.isEmpty()) {
                this.currentMainMenuScreen = MainMenuScreen.TRAIL_SELECTION;
            }
            SoundEngine.loadSFX(Audio.CLICK_1);
        }

        if (this.renderHomeButton()) {
            this.saveName.clear();
        }
        Format.clearFontSize();
    }

    private void renderSaves() {
        // Render the load save menu
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImVec2 loadGameTitlePos = RenderUtils.getCursorRelative(0, 0.025F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.main.loadSave")).x, loadGameTitlePos.y);
        ImGui.text(Translations.getTranslatedText("menu.main.loadSave"));
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);

        ImVec2 savesChildPos = RenderUtils.getCursorRelative(0.05F, 0.15F);
        ImVec2 savesChildSize = RenderUtils.getCursorRelative(0.9F, 0.8F);
        ImGui.setCursorPos(savesChildPos);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, ImColor.rgb(50, 50, 50));
        ImGui.beginChild("savesChild", savesChildSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        ImVec2 buttonSize = new ImVec2(RenderUtils.getCursorRelative(0.1F, 0.0F).x, ImGui.calcTextSizeY("0"));
        ImVec2 deleteButtonSize = new ImVec2(ImGui.calcTextSize("X").plus(10, 0));
        for (int i = 0; i < Registry.getLoadedAssetsCount(Registry.AssetType.SAVE); i++) {
            String[] data = Registry.getSaveData(i);
            ImGui.setCursorPos(savesChildSize.x * 0.02F, savesChildSize.y * 0.1F + (0.1F * savesChildSize.y * i));
            ImGui.text(data[0]);
            ImGui.sameLine(savesChildSize.x * 0.35F);
            ImGui.text(data[1]);
            ImGui.sameLine(savesChildSize.x * 0.8F);
            if (ImGui.button(Translations.getTranslatedText("menu.main.load") + "##" + i, buttonSize)) {
                SaveData save = (SaveData) Registry.getAsset(Registry.AssetType.SAVE, String.valueOf(i));
                Game.resetInstance(save);
                this.currentStage = GameStage.GAME;
                DebugLogger.info("Loaded Game");
                SoundEngine.loadSFX(Audio.CLICK_1);
            }
            ImGui.sameLine(savesChildSize.x * 0.93F);
            if (ImGui.button("X", deleteButtonSize)) {
                Registry.removeSaveData(i);
                SoundEngine.loadSFX(Audio.CLICK_2);
            }
        }
        ImGui.endChild();
        ImGui.popStyleColor();

        this.renderHomeButton();
        Format.clearFontSize();
    }

    private void renderTrailSelection() {
        // Render the game configuration menu
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImVec2 configGameTitlePos = RenderUtils.getCursorRelative(0, 0.025F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.trailSelection.gameConfig")).x, configGameTitlePos.y);
        ImGui.text(Translations.getTranslatedText("menu.trailSelection.gameConfig"));
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);

        ImVec2 trailButton1Pos = RenderUtils.getCursorRelative(0, 0.1F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("menu.trailSelection.easyTrail")) + 10F, trailButton1Pos.y);
        if (ImGui.radioButton(Translations.getTranslatedText("menu.trailSelection.easyTrail"), this.difficulty, 1)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
        }

        ImVec2 trailButton2Pos = RenderUtils.getCursorRelative(0, 0.2F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("menu.trailSelection.medTrail")) + 10F, trailButton2Pos.y);
        if (ImGui.radioButton(Translations.getTranslatedText("menu.trailSelection.medTrail"), this.difficulty, 2)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
        }

        ImVec2 trailButton3Pos = RenderUtils.getCursorRelative(0, 0.3F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("menu.trailSelection.hardTrail")) + 10F, trailButton3Pos.y);
        if (ImGui.radioButton(Translations.getTranslatedText("menu.trailSelection.hardTrail"), this.difficulty, 3)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
        }

        ImVec2 separatorPos = RenderUtils.getCursorRelative(0, 0.38F);
        ImGui.setCursorPosY(separatorPos.y);
        ImGui.separator();

        ImVec2 advancedHeaderPos = RenderUtils.getCursorRelative(0, 0.45F);
        ImVec2 advancedHeaderSize = RenderUtils.getCursorRelative(0.8F, 0.3F);
        RenderUtils.centeredHorizontal(advancedHeaderSize.x, advancedHeaderPos.y);
        ImGui.beginChild("advancedChild", advancedHeaderSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        if (ImGui.collapsingHeader(Translations.getTranslatedText("menu.trailSelection.advanced"))) {
            ImVec2 startingMoneyLabel = RenderUtils.getCursorRelative(0.1F, 0.1F);
            ImGui.setCursorPos(startingMoneyLabel);
            ImGui.text(Translations.getTranslatedText("menu.trailSelection.startingMoney"));
            ImGui.sameLine();
            ImVec2 moneyText = RenderUtils.getCursorRelative(0.15F, 0);
            ImGui.setNextItemWidth(moneyText.x);
            ImGui.inputText("##staringMoney", this.money, ImGuiInputTextFlags.CallbackEdit, this.inputMoneyMask);

            ImVec2 startingDayLabel = RenderUtils.getCursorRelative(0.1F, 0.2F);
            ImGui.setCursorPos(startingDayLabel);
            ImGui.text(Translations.getTranslatedText("menu.trailSelection.startingDay"));
            ImGui.sameLine();
            ImVec2 monthWidth = RenderUtils.getCursorRelative(0.2F, 0);
            ImGui.setNextItemWidth(monthWidth.x);
            ImGui.combo("##Month", this.month, MONTHS);
            ImGui.sameLine();
            ImVec2 dayWidth = RenderUtils.getCursorRelative(0.05F, 0);
            ImGui.setNextItemWidth(dayWidth.x);
            ImGui.inputText("##dayInput", this.day, ImGuiInputTextFlags.CallbackEdit, this.inputDayMask);
        }
        ImGui.endChild();

        ImVec2 continueButtonPos = RenderUtils.getCursorRelative(0, 0.85F);
        ImVec2 continueButtonSize = RenderUtils.getCursorRelative(0.2F, 0.1F);
        RenderUtils.centeredHorizontal(continueButtonSize.x, continueButtonPos.y);
        if (ImGui.button(Translations.getTranslatedText("menu.trailSelection.continue"), continueButtonSize.x, continueButtonSize.y)) {
            this.currentMainMenuScreen = MainMenuScreen.MEMBER_SELECTION;
            SoundEngine.loadSFX(Audio.CLICK_1);
        }

        this.renderBackButton(MainMenuScreen.NEW_GAME);
        Format.clearFontSize();
    }

    private void renderMemberSelection() {
        // Render the member configuration menu
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImVec2 memberTitlePos = RenderUtils.getCursorRelative(0, 0.025F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.memberSelection.members")).x, memberTitlePos.y);
        ImGui.text(Translations.getTranslatedText("menu.memberSelection.members"));
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);

        ImVec2 carouselSize = RenderUtils.getCursorRelative(0.8F, 0.49F);
        ImVec2 carouselPos = RenderUtils.getCursorRelative(0, 0.2F);
        RenderUtils.centeredHorizontal(carouselSize.x, carouselPos.y);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, ImColor.rgb(50, 50, 50));
        this.renderMemberInfoCarousel(carouselSize.x, carouselSize.y);
        ImGui.popStyleColor();

        ImVec2 buttonPos = RenderUtils.getCursorRelative(0, 0.85F);
        ImVec2 buttonSize = RenderUtils.getCursorRelative(0.3F, 0.1F);
        RenderUtils.centeredHorizontal(buttonSize.x * 2, buttonPos.y);
        ImGui.beginGroup();
        if (ImGui.button(Translations.getTranslatedText("menu.memberSelection.addMember"), buttonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            if (MEMBER_DATA.size() < Config.getConfig().getTeamMemberCount()) {
                MEMBER_DATA.add(new MemberPlaqueData(
                        new ImString(Translations.getTranslatedText("menu.memberSelection.name"), 16),
                        new ImInt(0),
                        new ImInt(0)
                ));
            }
        }
        ImGui.sameLine();
        if (ImGui.button(Translations.getTranslatedText("menu.memberSelection.startGame"), buttonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            DebugLogger.info("Starting Game");
            this.initializeGame();
            this.saveName.clear();
            this.day.clear();
            this.month.set(3);
            MEMBER_DATA.clear();
            this.money.clear();
            this.difficulty.set(1);
        }
        ImGui.endGroup();

        this.renderBackButton(MainMenuScreen.TRAIL_SELECTION);
        Format.clearFontSize();
    }

    private void renderLangSelection() {
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImVec2 langTitlePos = RenderUtils.getCursorRelative(0, 0.025F);
        RenderUtils.centeredHorizontal(ImGui.calcTextSize(Translations.getTranslatedText("menu.main.selectLang")).x, langTitlePos.y);
        ImGui.text(Translations.getTranslatedText("menu.main.selectLang"));
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);

        String[] langs = Config.getConfig().getLanguagesEnabled().keySet().toArray(new String[0]);
        String[] translatedLangNames = Config.getConfig().getLanguagesEnabled().values().toArray(new String[0]);
        for (int i = 0; i < langs.length; i++) {
            // Render language buttons
            ImVec2 langButtonPos = RenderUtils.getCursorRelative(0.4F, 0.15F + 0.15F * (float) i);
            ImVec2 langButtonSize = RenderUtils.getCursorRelative(0.2F, 0.1F);
            ImGui.setCursorPos(langButtonPos);
            if (ImGui.button(translatedLangNames[i], langButtonSize.x, langButtonSize.y)) {
                SoundEngine.loadSFX(Audio.CLICK_2);

                // Save new language to config file
                Config.getConfig().setLanguage(langs[i]);
                Config.getConfig().saveConfig();
                DebugLogger.info("Saved config with new language {}.", langs[i]);

                // Reload translations with the new language
                Translations.loadTranslations(langs[i]);
            }
        }

        this.renderHomeButton();
        Format.clearFontSize();
    }

    /**
     * Renders the wagon on the cursor
     */
    public void renderCursor() {
        ImVec2 pMin = ImGui.getMousePos().minus(24, 24);
        ImVec2 pMax = ImGui.getMousePos().plus(24, 24);
        switch (this.cursorType) {
            case STANDARD -> ImGui.setMouseCursor(ImGuiMouseCursor.Arrow);
            case WAGON -> {
                ImGui.setMouseCursor(ImGuiMouseCursor.None);
                ImGui.getForegroundDrawList().addImage((Long) Registry.getAsset(Registry.AssetType.ASSET, "cursor"), pMin, pMax);
            }
            case FINGER -> ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
            case RIFLE -> {
                ImGui.setMouseCursor(ImGuiMouseCursor.None);
                ImGui.getForegroundDrawList().addImage((Long) Registry.getAsset(Registry.AssetType.ASSET, "cursorRifle"), pMin, pMax);
            }
        }
    }

    private boolean renderHomeButton() {
        boolean pressed = false;
        ImVec2 renderHomeButtonPos = RenderUtils.getCursorRelative(0.93F, 0.0125F);
        ImGui.setCursorPos(renderHomeButtonPos.x, renderHomeButtonPos.y);
        ImGui.pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0.6F, 0.1F);
        ImVec2 renderHomeButtonSize = RenderUtils.getItemScaleMaintainAspectRatio(0.075F, 0.06F);
        if (ImGui.button("X", renderHomeButtonSize.x, renderHomeButtonSize.y)) {
            this.currentMainMenuScreen = MainMenuScreen.MENU;
            pressed = true;
            SoundEngine.loadSFX(Audio.CLICK_2);
        }
        ImGui.popStyleVar();
        return pressed;
    }

    private void renderBackButton(MainMenuScreen previousScreen) {
        ImVec2 renderBackButtonPos = RenderUtils.getCursorRelative(0.93F, 0.0125F);
        ImGui.setCursorPos(renderBackButtonPos.x, renderBackButtonPos.y);
        ImGui.pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0.6F, 0.1F);
        ImVec2 renderBackButtonSize = RenderUtils.getItemScaleMaintainAspectRatio(0.075F, 0.06F);
        if (ImGui.button("<", renderBackButtonSize.x, renderBackButtonSize.y)) {
            this.currentMainMenuScreen = previousScreen;
            SoundEngine.loadSFX(Audio.CLICK_2);
        }
        ImGui.popStyleVar();
    }

    private boolean renderMemberInfoPlaque(int index) {
        ImVec2 plaqueSize = RenderUtils.getCursorRelative(0.4F, 0.6F);
        ImGui.setNextItemWidth(plaqueSize.x);
        ImGui.beginGroup();

        ImVec2 startPos = ImGui.getCursorScreenPos();

        if (index == 0) {
            ImGui.text(Translations.getTranslatedText("memberInfoPlaque.you"));
            ImGui.sameLine();
            ImGui.setNextItemWidth(plaqueSize.x - ImGui.calcTextSizeX(Translations.getTranslatedText("memberInfoPlaque.you")));
        } else {
            ImGui.setNextItemWidth(plaqueSize.x);
        }
        ImGui.inputText("##MemberName" + index, MEMBER_DATA.get(index).name());
        ImVec2 radioButton1Pos = RenderUtils.getCursorRelative(0F, 0.15F);
        ImGui.setCursorPosY(radioButton1Pos.y);
        if (ImGui.radioButton(Translations.getTranslatedText("memberInfoPlaque.male") + "##" + index, MEMBER_DATA.get(index).gender(), 0)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
        }
        ImGui.sameLine();
        if (ImGui.radioButton(Translations.getTranslatedText("memberInfoPlaque.female") + "##" + index, MEMBER_DATA.get(index).gender(), 1)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
        }
        ImVec2 rolePos = RenderUtils.getCursorRelative(0, 0.25F);
        ImGui.setCursorPosY(rolePos.y);
        ImGui.text(Translations.getTranslatedText("memberInfoPlaque.role"));
        ImGui.sameLine();
        ImVec2 roleComboSize = RenderUtils.getCursorRelative(0.24F, 0F);
        ImGui.setNextItemWidth(roleComboSize.x);
        String[] currentRoles = MEMBER_DATA.get(index).gender().get() == 0 ? MALE_ROLES : FEMALE_ROLES;
        if (MEMBER_DATA.get(index).role().get() >= currentRoles.length) {
            MEMBER_DATA.get(index).role().set(currentRoles.length - 1);
        }
        if (ImGui.combo("##Role" + index, MEMBER_DATA.get(index).role(), currentRoles)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
        }
        ImVec2 buttonsPos = RenderUtils.getCursorRelative(0, 0.40F);
        ImGui.setCursorPosY(buttonsPos.y);
        if (ImGui.button(Translations.getTranslatedText("memberInfoPlaque.randomize") + "##" + index)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            Member random = Member.randomize();
            MEMBER_DATA.get(index).name().set(random.getName());
            MEMBER_DATA.get(index).gender().set(random.getGender() == Gender.MALE ? 0 : 1);
            MEMBER_DATA.get(index).role().set(Registry.indexOf(Registry.AssetType.ROLE, random.getRole().id()));
        }
        ImGui.sameLine();
        if (ImGui.button(Translations.getTranslatedText("memberInfoPlaque.delete") + "##" + index)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
            ImGui.endGroup();
            return false;
        }
        ImGui.endGroup();

        ImVec2 endPos = ImGui.getItemRectSize();

        ImGui.getWindowDrawList().addRect(startPos, new ImVec2(startPos.x + endPos.x, startPos.y + endPos.y), ImGui.getColorU32(ImGuiCol.Border));

        return true;
    }

    /**
     * If the Member data contains a member
     * @param name The Member's name
     * @return if the member data contains that name
     */
    public static boolean containsName(String name) {
        for (MemberPlaqueData member : MEMBER_DATA) {
            if (member.name().get().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void renderMemberInfoCarousel(float width, float height) {
        ImVec2 childSize = new ImVec2(width, height);

        ImGui.beginChild("memberInfoCarousel", childSize, false, ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);

        ListIterator<MemberPlaqueData> nameIter = MEMBER_DATA.listIterator();

        ImVec2 plaquePos = RenderUtils.getCursorRelative(0.02F, 0.03F);
        ImGui.setCursorPos(plaquePos);

        while (nameIter.hasNext()) {
            int i = nameIter.nextIndex();
            nameIter.next();
            boolean sameLine = i < MEMBER_DATA.size() -1;

            if (!renderMemberInfoPlaque(i)) {
                nameIter.remove();
            }

            if (sameLine) {
                ImGui.sameLine();
            }
        }

        ImGui.endChild();
    }

    /**
     * Sets the type of cursor to display
     * @param cursorType CursorType instance
     */
    public void setCursorType(CursorType cursorType) {
        this.cursorType = cursorType;
    }

    /**
     * They image to render the cursor as
     */
    public enum CursorType {
        /**
         * Basic arrow cursor
         */
        STANDARD,
        /**
         * Oregon Trail wagon
         */
        WAGON,
        /**
         * Pointing cursor
         */
        FINGER,
        /**
         * Gun cursor for hunting
         */
        RIFLE,
    }

    enum GameStage {
        MAIN_MENU,
        GAME,
    }

    enum MainMenuScreen {
        MENU,
        SETTINGS,
        LOAD_SAVE,
        NEW_GAME,
        TRAIL_SELECTION, // Determines difficulty
        MEMBER_SELECTION, // Add yourself, then members
        LANG_SELECTION, // Language selection
    }

    /**
     * Reloads all translation caches in Renderer
     */
    public static void reloadTranslations() {
        // Reload months
        String[] months = Date.Month.getMonths();
        System.arraycopy(months, 0, MONTHS, 0, MONTHS.length);
        String[] rolesMale = Role.getRoles(Gender.MALE);
        String[] rolesFemale = Role.getRoles(Gender.FEMALE);
        System.arraycopy(rolesFemale, 0, FEMALE_ROLES, 0, FEMALE_ROLES.length);
        System.arraycopy(rolesMale, 0, MALE_ROLES, 0, MALE_ROLES.length);
    }

    private void initializeGame() {
        SaveData.SaveDataBuilder builder = new SaveData.SaveDataBuilder();
        builder.difficulty(this.difficulty.shortValue());
        if (this.money.getLength() > 1) {
            builder.money(Float.parseFloat(this.money.get().replace('$', ' ')));
        }
        if (this.day.isNotEmpty() && this.month.get() != 0) {
            builder.date(Integer.parseInt(this.day.get()), this.month.get());
        }
        builder.saveName(this.saveName.get());
        for (int i = 0; i < Config.getConfig().getTeamMemberCount(); i++) {
            if (MEMBER_DATA.size() > i) {
                MemberPlaqueData data = MEMBER_DATA.get(i);
                if (data.isValid()) {
                    builder.addMember(data);
                } else {
                    builder.addMember();
                }
            } else {
                builder.addMember();
            }
        }

        Game.resetInstance(builder.build());
        this.currentStage = GameStage.GAME;
    }

    /**
     * Sets the current menu to the main menu
     */
    public void returnToMainMenu() {
        this.currentMainMenuScreen = MainMenuScreen.MENU;
        this.currentStage = GameStage.MAIN_MENU;
    }

    /**
     * Creates and displays a Consequences Screen
     * @param translationKey Consequences Screen translation key (notification translation for now)
     * @param data Additional data to add to the translation
     */
    public static void addConsequence(String translationKey, String... data) {
        ConsequenceScreen screen = (ConsequenceScreen) Registry.getAsset(Registry.AssetType.SCREEN, "consequence");
        if (data.length > 0) {
            screen.setData(Translations.getTranslatedText(translationKey, data));
        } else {
            screen.setData(Translations.getTranslatedText(translationKey));
        }
        Renderer.RENDER_QUEUE.add("consequence");
    }
}

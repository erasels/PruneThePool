package PruneThePool;

import PruneThePool.ui.PruneCounter;
import basemod.*;
import basemod.abstracts.CustomSavable;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.StartGameSubscriber;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

@SpireInitializer
public class PruneThePool implements
        PostInitializeSubscriber,
        PostBattleSubscriber,
        EditStringsSubscriber,
        StartGameSubscriber {
    public static final Logger pruneLogger = LogManager.getLogger(PruneThePool.class);
    private static SpireConfig modConfig = null;
    public static PruneCounter pruneBtn;

    public static void initialize() {
        BaseMod.subscribe(new PruneThePool());

        try {
            Properties defaults = new Properties();
            defaults.put("CompleteRemoval", Boolean.toString(false));
            defaults.put("StartingCharges", Integer.toString(5));
            defaults.put("EliteCharges", Integer.toString(1));
            defaults.put("BossCharges", Integer.toString(3));
            modConfig = new SpireConfig("PruneThePool", "Config", defaults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean shouldCR() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("CompleteRemoval");
    }

    public static int getSC() {
        if (modConfig == null) {
            return -1;
        }
        return modConfig.getInt("StartingCharges");
    }

    public static int getEC() {
        if (modConfig == null) {
            return -1;
        }
        return modConfig.getInt("EliteCharges");
    }

    public static int getBC() {
        if (modConfig == null) {
            return -1;
        }
        return modConfig.getInt("BossCharges");
    }

    private int yPos = 700;
    private ModPanel settingsPanel = new ModPanel();
    @Override
    public void receivePostInitialize() {
        UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("Config"));

        int xPos = 350;
        ModLabeledToggleButton CRBtn = new ModLabeledToggleButton(uiStrings.TEXT_DICT.get("CR"), xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, shouldCR(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("CompleteRemoval", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        registerUIElement(CRBtn, true);

        ModLabel SCSliderLabel = new ModLabel(uiStrings.TEXT_DICT.get("StartingCharge"), xPos + 40, yPos + 8, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, l -> { });
        registerUIElement(SCSliderLabel, false);
        float textWidth = FontHelper.getWidth(FontHelper.charDescFont, uiStrings.TEXT_DICT.get("EliteCharge"), 1f / Settings.scale);

        ModMinMaxSlider SCSlider = new ModMinMaxSlider("", xPos + 100 + textWidth, yPos + 15, 0, 25, getSC(), "x%2.0f", settingsPanel, slider -> {
            if (modConfig != null) {
                modConfig.setInt("StartingCharges", MathUtils.round(slider.getValue()));
                saveConfig();
            }
        });
        registerUIElement(SCSlider, true);

        ModLabel ECSliderLabel = new ModLabel(uiStrings.TEXT_DICT.get("EliteCharge"), xPos + 40, yPos + 8, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, l -> { });
        registerUIElement(ECSliderLabel, false);

        ModMinMaxSlider ECSlider = new ModMinMaxSlider("", xPos + 100 + textWidth, yPos + 15, 0, 10, getEC(), "x%2.0f", settingsPanel, slider -> {
            if (modConfig != null) {
                modConfig.setInt("EliteCharges", MathUtils.round(slider.getValue()));
                saveConfig();
            }
        });
        registerUIElement(ECSlider, true);

        ModLabel BCSliderLabel = new ModLabel(uiStrings.TEXT_DICT.get("BossCharge"), xPos + 40, yPos + 8, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, l -> { });
        registerUIElement(BCSliderLabel, false);

        ModMinMaxSlider BCSlider = new ModMinMaxSlider("", xPos + 100 + textWidth, yPos + 15, 0, 10, getBC(), "x%2.0f", settingsPanel, slider -> {
            if (modConfig != null) {
                modConfig.setInt("BossCharges", MathUtils.round(slider.getValue()));
                saveConfig();
            }
        });
        registerUIElement(BCSlider, true);

        BaseMod.registerModBadge(ImageMaster.loadImage("pruneThePoolResources/img/modBadge.png"), "PruneThePool", "erasels", "TODO", settingsPanel);

        if(pruneBtn == null) {
            pruneBtn = new PruneCounter();
        }
        BaseMod.addTopPanelItem(pruneBtn);

        BaseMod.addSaveField("PtP_PruneCharges", new CustomSavable<Integer>() {
            @Override
            public Integer onSave() {
                return PruneCounter.charges;
            }

            @Override
            public void onLoad(Integer i) {
                if(i != null) {
                    pruneBtn.setCharge(i);
                }
            }
        });

        BaseMod.addSaveField("PtP_PrunedCards", new CustomSavable<ArrayList<String>>() {
            @Override
            public ArrayList<String> onSave() {
                return PruneCounter.prunedCards;
            }

            @Override
            public void onLoad(ArrayList<String> list) {
                if(list != null) {
                    PruneCounter.prunedCards = list;
                }
            }
        });
    }

    private void registerUIElement(IUIElement elem, boolean decrement) {
        settingsPanel.addUIElement(elem);

        if (decrement) {
            yPos -= 50;
        }
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        if(abstractRoom instanceof MonsterRoomElite) {
            pruneBtn.gainCharge(PruneCounter.ELITE_CHARGES);
        }
        if(abstractRoom instanceof MonsterRoomBoss) {
            pruneBtn.gainCharge(PruneCounter.BOSS_CHARGES);
        }
    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/loc/" + locPath() + "/uiStrings.json");
    }

    @Override
    public void receiveStartGame() {
        PruneCounter.START_CHARGES = getSC();
        PruneCounter.ELITE_CHARGES = getEC();
        PruneCounter.BOSS_CHARGES = getBC();

        if(!CardCrawlGame.loadingSave) {
            pruneBtn.setCharge(PruneCounter.START_CHARGES);
            PruneCounter.prunedCards.clear();
        }
    }

    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/img/ui/" + resourcePath;
    }

    private String locPath() {
        if(Settings.language == Settings.GameLanguage.RUS) {
            return "rus";
        } else if (Settings.language == Settings.GameLanguage.ZHS) {
            return "zhs";
        }
        return "eng";
    }

    public static String getModID() {
        return "pruneThePool";
    }

    public static String makeID(String input) {
        return getModID() + ":" + input;
    }

    private void saveConfig() {
        try {
            modConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
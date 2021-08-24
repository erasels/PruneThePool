package PruneThePool;

import PruneThePool.ui.PruneCounter;
import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.abstracts.CustomSavable;
import basemod.interfaces.*;
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

    @Override
    public void receivePostInitialize() {
        ModPanel settingsPanel = new ModPanel();

        UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("Config"));

        ModLabeledToggleButton CRBtn = new ModLabeledToggleButton(uiStrings.TEXT_DICT.get("CR"), 350, 700, Settings.CREAM_COLOR, FontHelper.charDescFont, shouldCR(), settingsPanel, l -> {
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
        settingsPanel.addUIElement(CRBtn);

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

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        if(abstractRoom instanceof MonsterRoomElite) {
            pruneBtn.gainCharge(2);
        }
        if(abstractRoom instanceof MonsterRoomBoss) {
            pruneBtn.gainCharge(3);
        }
    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/loc/" + locPath() + "/uiStrings.json");
    }

    @Override
    public void receiveStartGame() {
        if(!CardCrawlGame.loadingSave) {
            pruneBtn.setCharge(PruneCounter.START_CHARGES);
            PruneCounter.prunedCards.clear();
        }
    }

    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/img/ui/" + resourcePath;
    }

    private String locPath() {
        return "eng";
    }

    public static String getModID() {
        return "pruneThePool";
    }

    public static String makeID(String input) {
        return getModID() + ":" + input;
    }
}
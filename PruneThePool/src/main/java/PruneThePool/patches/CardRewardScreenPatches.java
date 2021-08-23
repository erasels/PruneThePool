package PruneThePool.patches;

import PruneThePool.ui.PruneButton;
import PruneThePool.ui.PruneCounter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class CardRewardScreenPatches {
    public static ArrayList<PruneButton> buttons = new ArrayList<>();

    @SpirePatch2(clz = CardRewardScreen.class, method = "open")
    public static class GenerateButtons {
        @SpirePostfixPatch
        public static void patch(CardRewardScreen __instance) {
            for(int i = 0; i < __instance.rewardGroup.size(); i++) {
                PruneButton bt = new PruneButton(i);
                buttons.add(bt);
                bt.show();
            }
        }
    }

    @SpirePatch2(clz = CardRewardScreen.class, method = "onClose")
    public static class YeetButtons {
        @SpirePostfixPatch
        public static void patch() {
            buttons.clear();
        }
    }

    @SpirePatch2(clz = CardRewardScreen.class, method = "render")
    public static class RenderButtons {
        @SpirePostfixPatch
        public static void patch(SpriteBatch sb) {
            buttons.forEach(b -> b.render(sb));
        }
    }

    @SpirePatch2(clz = CardRewardScreen.class, method = "update")
    public static class UpdateButtons {
        @SpirePostfixPatch
        public static void patch() {
            buttons.forEach(PruneButton::update);
        }
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "initializeCardPools")
    public static class CardpoolInitFix {
        @SpirePostfixPatch
        public static void patch() {
            for(String s : PruneCounter.prunedCards) {
                AbstractDungeon.srcColorlessCardPool.removeCard(s);
                AbstractDungeon.srcCommonCardPool.removeCard(s);
                AbstractDungeon.srcUncommonCardPool.removeCard(s);
                AbstractDungeon.srcRareCardPool.removeCard(s);

                AbstractDungeon.colorlessCardPool.removeCard(s);
                AbstractDungeon.commonCardPool.removeCard(s);
                AbstractDungeon.uncommonCardPool.removeCard(s);
                AbstractDungeon.rareCardPool.removeCard(s);
            }
        }
    }

    public static boolean rollSingle = false;
    @SpirePatch2(clz = AbstractDungeon.class, method = "getRewardCards")
    public static class RolleSingleCard {
        @SpireInsertPatch(locator = Locator.class, localvars = {"numCards"})
        public static void patch(@ByRef int[] numCards) {
            if(rollSingle) {
                numCards[0] = 1;
                rollSingle = false;
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractDungeon.class, "rollRarity");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}

package PruneThePool.patches;

import PruneThePool.PruneThePool;
import PruneThePool.ui.LabledButton;
import PruneThePool.ui.PruneButton;
import PruneThePool.ui.PruneCounter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class CardRewardScreenPatches {
    public static ArrayList<LabledButton> buttons = new ArrayList<>();

    //Add buttons to cards in card reward
    @SpirePatch2(clz = CardRewardScreen.class, method = "open")
    public static class GenerateButtons {
        @SpirePostfixPatch
        public static void patch(CardRewardScreen __instance) {
            LabledButton btn;
            for(int i = 0; i < __instance.rewardGroup.size(); i++) {
                btn = new PruneButton(i);
                buttons.add(btn);
                btn.show();
            }
        }
    }

    //Remove buttons when done
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
            buttons.forEach(LabledButton::update);
        }
    }

    //Removes relevant card from the pools which get refreshed whenever a new act is entered
    @SpirePatch2(clz = AbstractDungeon.class, method = "initializeCardPools")
    public static class CardpoolInitFix {
        @SpirePostfixPatch
        public static void patch() {
            for(String s : PruneCounter.prunedCards) {
                PruneThePool.pruneBtn.pruneCard(s, false);
            }
        }
    }

    //Allows for rolling a single card with the card reward generation method
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

    //Prevents the newly spawned card from being a dupe of the existing cards by merging and seperating the lists when needed
    public static boolean modifiedRetval = false;
    @SpirePatch2(clz = AbstractDungeon.class, method = "getRewardCards")
    public static class PrecentDupeReplacement {
        @SpireInsertPatch(locator = PostRetvalLocator.class, localvars = {"retVal"})
        public static void addCards(ArrayList<AbstractCard> retVal) {
            if(rollSingle) {
                modifiedRetval = true;
                retVal.addAll(AbstractDungeon.cardRewardScreen.rewardGroup);
            }
        }

        @SpireInsertPatch(locator = PreRetval2Locator.class, localvars = {"retVal"})
        public static void removeCards(ArrayList<AbstractCard> retVal) {
            if(modifiedRetval) {
                retVal.removeAll(AbstractDungeon.cardRewardScreen.rewardGroup);
                modifiedRetval = false;
            }
        }

        private static class PostRetvalLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "relics");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }

        private static class PreRetval2Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "add");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}

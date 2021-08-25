package PruneThePool.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class CardRerollPatches {
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
    @SpirePatch2(clz = AbstractDungeon.class, method = "getColorlessRewardCards")
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

    //Mark colorless card rewards
    @SpirePatch(clz = RewardItem.class, method = SpirePatch.CLASS)
    public static class Fields {
        public static SpireField<Boolean> colorlessCardReward = new SpireField<>(()->false);
    }

    @SpirePatch2(clz = RewardItem.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.CardColor.class})
    public static class MarkAsColorlessReward {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(RewardItem __instance) {
            Fields.colorlessCardReward.set(__instance, true);
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractDungeon.class, "getColorlessRewardCards");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    public static boolean colorlessOrigin = false;
    @SpirePatch2(clz = RewardItem.class, method = "claimReward")
    public static class MarkScreenAsColorless {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(RewardItem __instance) {
            if(Fields.colorlessCardReward.get(__instance)) {
                colorlessOrigin = true;
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(CardRewardScreen.class, "open");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch2(clz = CardRewardScreen.class, method = "onClose")
    public static class UnmarkOrigin {
        @SpirePostfixPatch
        public static void patch() {
            colorlessOrigin = false;
        }
    }
}

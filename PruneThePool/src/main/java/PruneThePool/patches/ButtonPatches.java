package PruneThePool.patches;

import PruneThePool.PruneThePool;
import PruneThePool.ui.LabledButton;
import PruneThePool.ui.PruneButton;
import PruneThePool.ui.PruneCounter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.CardRewardScreen;

import java.util.ArrayList;

public class ButtonPatches {
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
}

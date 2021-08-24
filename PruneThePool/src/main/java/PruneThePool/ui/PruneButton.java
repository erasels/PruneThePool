package PruneThePool.ui;

import PruneThePool.PruneThePool;
import PruneThePool.patches.CardRewardScreenPatches;
import PruneThePool.util.UC;
import PruneThePool.vfx.BetterSmokeBombEffect;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;

public class PruneButton extends LabledButton{
    private static UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(PruneThePool.makeID("PruneButton"));
    public int slot;

    public PruneButton(int slot) {
        super(0, 0, uiStrings.TEXT[0], false, () -> {}, Color.LIGHT_GRAY, Color.WHITE);
        this.slot = slot;

        x = current_x = target_x = pointer().current_x;
        y = current_y = target_y = pointer().target_y + (AbstractCard.RAW_H/2f * Settings.scale);

        exec = () -> {
            PruneThePool.pruneBtn.useCharge();
            PruneThePool.pruneBtn.pruneCard(pointer().cardID);

            AbstractDungeon.topLevelEffects.add(new BetterSmokeBombEffect(pointer().hb.cX, pointer().hb.cY));

            CardRewardScreenPatches.rollSingle = true;
            AbstractCard newCard = AbstractDungeon.getRewardCards().get(0);
            UC.copyCardPosition(pointer(), newCard);
            AbstractDungeon.cardRewardScreen.rewardGroup.set(slot, newCard);
        };
    }

    @Override
    public void update() {
        if(PruneCounter.charges > 0) {
            if(isHidden) {
                show();
            }
            x = target_x = pointer().current_x;
            y = target_y = pointer().target_y + pointer().hb.height / 2f + hb.height / 2f;
            super.update();
        } else {
            hide();
        }
    }

    @Override
    protected void onHoverRender(SpriteBatch sb) {
        TipHelper.renderGenericTip(InputHelper.mX + 50f * Settings.scale, InputHelper.mY, uiStrings.TEXT[0], uiStrings.TEXT[1]);
    }

    private AbstractCard pointer() {
        return AbstractDungeon.cardRewardScreen.rewardGroup.get(slot);
    }
}

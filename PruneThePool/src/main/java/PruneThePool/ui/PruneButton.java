package PruneThePool.ui;

import PruneThePool.PruneThePool;
import PruneThePool.patches.ButtonPatches;
import PruneThePool.patches.CardRerollPatches;
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

import java.util.ArrayList;

public class PruneButton extends LabledButton {
    private static final float ANIMATOR_Y_OFFSET = LabledButton.HITBOX_H;
    protected static UIStrings pruneStrings = CardCrawlGame.languagePack.getUIString(PruneThePool.makeID("PruneButton"));
    protected static UIStrings pushStrings = CardCrawlGame.languagePack.getUIString(PruneThePool.makeID("PushButton"));
    public int slot;
    public boolean isPrune;

    private String lastCardID;

    public PruneButton(int slot) {
        super(0, 0, pruneStrings.TEXT[0], false, () -> {
        }, Color.LIGHT_GRAY, Color.WHITE);
        this.slot = slot;

        lastCardID = pointer().cardID;
        updateStatus();

        x = current_x = target_x = pointer().current_x;
        y = current_y = target_y = pointer().target_y + (AbstractCard.RAW_H / 2f * Settings.scale);

        if(ButtonPatches.animatorWorkaround) {
            y = current_y = target_y = target_y + ANIMATOR_Y_OFFSET;
        }

        exec = () -> {
            PruneThePool.pruneBtn.useCharge();
            if (isPrune) {
                PruneThePool.pruneBtn.pruneCard(pointer().cardID);
            }

            AbstractDungeon.topLevelEffects.add(new BetterSmokeBombEffect(pointer().hb.cX, pointer().hb.cY));

            AbstractCard newCard;
            CardRerollPatches.rollSingle = true;
            if (CardRerollPatches.colorlessOrigin) {
                newCard = AbstractDungeon.getColorlessRewardCards().get(0);
            } else {
                newCard = AbstractDungeon.getRewardCards().get(0);
            }
            UC.copyCardPosition(pointer(), newCard);
            AbstractDungeon.cardRewardScreen.rewardGroup.set(slot, newCard);

            updateStatus();
        };
    }

    @Override
    public void update() {
        if (PruneCounter.charges > 0) {
            AbstractCard c = pointer();
            if (!lastCardID.equals(c.cardID)) {
                updateStatus();
            }

            if (isHidden) {
                show();
            }
            x = target_x = c.current_x;
            y = target_y = c.target_y + c.hb.height / 2f + hb.height / 2f;

            if(ButtonPatches.animatorWorkaround) {
                y= target_y += ANIMATOR_Y_OFFSET;
            }
            super.update();
        } else {
            hide();
        }
    }

    @Override
    protected void onHoverRender(SpriteBatch sb) {
        String header, body;
        if (isPrune) {
            header = pruneStrings.TEXT[0];
            body = pruneStrings.TEXT[1];
        } else {
            header = pushStrings.TEXT[0];
            body = pushStrings.TEXT[1];
        }
        TipHelper.renderGenericTip(InputHelper.mX + 50f * Settings.scale, InputHelper.mY, header, body);
    }

    private void updateStatus() {
        ArrayList<String> poolCards = new ArrayList<>();
        AbstractDungeon.commonCardPool.group.forEach(c -> poolCards.add(c.cardID));
        AbstractDungeon.uncommonCardPool.group.forEach(c -> poolCards.add(c.cardID));
        AbstractDungeon.rareCardPool.group.forEach(c -> poolCards.add(c.cardID));
        if (poolCards.contains(pointer().cardID)) {
            isPrune = true;
        } else {
            isPrune = false;
        }

        updateName();
    }

    private void updateName() {
        if (isPrune) {
            msg = pruneStrings.TEXT[0];
        } else {
            msg = pushStrings.TEXT[0];
        }
    }

    private AbstractCard pointer() {
        return AbstractDungeon.cardRewardScreen.rewardGroup.get(slot);
    }
}

package PruneThePool.ui;

import PruneThePool.PruneThePool;
import PruneThePool.util.TextureLoader;
import PruneThePool.util.UC;
import basemod.TopPanelItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.DiscardPileViewScreen;

import java.util.ArrayList;

public class PruneCounter extends TopPanelItem {
    public static final int START_CHARGES = 5;
    private static final float FLASH_ANIM_TIME = 2.0F;
    private static final float tipYpos = Settings.HEIGHT - (120.0f * Settings.scale);
    public float flashTimer;

    public static final String ID = PruneThePool.makeID("PruneCounter");

    private static final Texture ICON = TextureLoader.getTexture(PruneThePool.makeUIPath("PruneCounter.png"));
    public static UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(ID);

    public static int charges = 0;
    public static ArrayList<String> prunedCards = new ArrayList<>();

    public static DiscardPileViewScreen backup;

    public PruneCounter() {
        super(ICON, ID);
        setClickable(true);
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        renderFlash(sb);
        FontHelper.cardTitleFont.getData().setScale(1.0f);
        FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, String.valueOf(charges), this.x + (this.hb_w / 2), this.y + 16f * Settings.scale, Color.WHITE.cpy());

        if (getHitbox().hovered) {
            TipHelper.renderGenericTip(this.x, tipYpos, uiStrings.TEXT[0], uiStrings.TEXT[1]);
        }
    }

    @Override
    protected void onClick() {
        if (!prunedCards.isEmpty() && AbstractDungeon.screen != AbstractDungeon.CurrentScreen.DISCARD_VIEW && !UC.isInCombat()) {
            CardCrawlGame.sound.play("STAB_BOOK_DEATH");
            CardGroup prunes = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            for (String s : prunedCards) {
                prunes.addToTop(CardLibrary.getCopy(s));
            }

            AbstractDungeon.previousScreen = AbstractDungeon.screen;
            backup = AbstractDungeon.discardPileViewScreen;
            AbstractDungeon.discardPileViewScreen = new ViewCardScreen(prunes.group);
            AbstractDungeon.discardPileViewScreen.open();
            //AbstractDungeon.gridSelectScreen.open(prunes, 1, uiStrings.TEXT_DICT.get("PruneScreen"), false, false, true, false);
        }
    }

    public void flash() {
        this.flashTimer = FLASH_ANIM_TIME;
    }

    @Override
    public void update() {
        updateFlash();
        super.update();
    }

    private void updateFlash() {
        if (flashTimer != 0.0F) {
            flashTimer -= Gdx.graphics.getDeltaTime();
        }
    }

    public void renderFlash(SpriteBatch sb) {
        float tmp = Interpolation.exp10In.apply(0.0F, 4.0F, flashTimer / FLASH_ANIM_TIME);
        sb.setBlendFunction(770, 1);
        sb.setColor(new Color(1.0F, 1.0F, 1.0F, flashTimer * FLASH_ANIM_TIME));

        float halfWidth = (float) this.image.getWidth() / 2.0F;
        float halfHeight = (float) this.image.getHeight() / 2.0F;
        sb.draw(this.image, this.x - halfWidth + halfHeight * Settings.scale, this.y - halfHeight + halfHeight * Settings.scale, halfWidth, halfHeight, (float) this.image.getWidth(), (float) this.image.getHeight(), Settings.scale + tmp, Settings.scale + tmp, this.angle, 0, 0, this.image.getWidth(), this.image.getHeight(), false, false);
        sb.draw(this.image, this.x - halfWidth + halfHeight * Settings.scale, this.y - halfHeight + halfHeight * Settings.scale, halfWidth, halfHeight, (float) this.image.getWidth(), (float) this.image.getHeight(), Settings.scale + tmp * 0.66F, Settings.scale + tmp * 0.66F, this.angle, 0, 0, this.image.getWidth(), this.image.getHeight(), false, false);
        sb.draw(this.image, this.x - halfWidth + halfHeight * Settings.scale, this.y - halfHeight + halfHeight * Settings.scale, halfWidth, halfHeight, (float) this.image.getWidth(), (float) this.image.getHeight(), Settings.scale + tmp / 3.0F, Settings.scale + tmp / 3.0F, this.angle, 0, 0, this.image.getWidth(), this.image.getHeight(), false, false);

        sb.setBlendFunction(770, 771);
    }

    public void gainCharge(int amt) {
        charges += amt;
        flash();
    }

    public void useCharge() {
        charges--;
        flash();
    }

    public void setCharge(int amt) {
        charges = amt;
    }

    public void pruneCard(String s, boolean addToPruneList) {
        AbstractDungeon.commonCardPool.removeCard(s);
        AbstractDungeon.uncommonCardPool.removeCard(s);
        AbstractDungeon.rareCardPool.removeCard(s);

        if(PruneThePool.shouldCR()) {
            AbstractDungeon.srcCommonCardPool.removeCard(s);
            AbstractDungeon.srcUncommonCardPool.removeCard(s);
            AbstractDungeon.srcRareCardPool.removeCard(s);
        }

        if(addToPruneList)
            addPrunedCard(s);
    }

    public void pruneCard(String s) {
        pruneCard(s, true);
    }

    public void addPrunedCard(String ID) {
        prunedCards.add(ID);
    }
}


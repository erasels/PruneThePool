package PruneThePool.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import org.apache.commons.lang3.math.NumberUtils;

public class LabledButton {
    private static final int W = 512;
    private static final int H = 256;
    public static final float TAKE_Y = Settings.HEIGHT / 2.0F - 340.0F * Settings.scale;
    private static final float SHOW_X = Settings.WIDTH / 2.0F;
    private static final float HIDE_X = -Settings.WIDTH;
    protected float current_x, current_y;
    protected float target_x, target_y;
    protected float x, y;
    protected boolean isHidden = true;
    protected String msg;
    private boolean toCancel;
    protected Runnable exec;
    private Color textColor = Color.WHITE.cpy();
    private Color btnColor, highColor;
    private float duration = 0;
    public boolean screenDisabled = false;
    private static final float HITBOX_W = 260.0F * Settings.scale;
    private static final float HITBOX_H = 80.0F * Settings.scale;
    public Hitbox hb = new Hitbox(0.0F, 0.0F, HITBOX_W, HITBOX_H);

    //x, y, text, cancel, exec
    public LabledButton(float x, float y, String msg, boolean toCancel, Runnable exec, Color col, Color highlight) {
        this.msg = msg;
        this.toCancel = toCancel;
        this.exec = exec;
        btnColor = col.cpy();
        highColor = highlight.cpy();
        this.hb.move(x, y);
        this.y = y;
        this.x = x;
        current_x = 0;
        target_x = x;
        current_y = 0;
        target_y = y;
    }

    public void update() {
        if (this.isHidden)
            return;
        this.hb.update();
        if (this.hb.justHovered)
            CardCrawlGame.sound.play("UI_HOVER");
        if (this.hb.hovered && InputHelper.justClickedLeft) {
            this.hb.clickStarted = true;
            CardCrawlGame.sound.play("UI_CLICK_1");
        }
        if ((this.hb.clicked || ((InputActionSet.cancel.isJustPressed() || CInputActionSet.cancel.isJustPressed()) && toCancel)) && !this.screenDisabled) {
            this.hb.clicked = false;
            exec.run();
        }

        this.screenDisabled = false;
        if (this.current_x != this.target_x) {
            this.current_x = Interpolation.linear.apply(current_x, target_x, NumberUtils.min(duration, 1f));
            duration += Gdx.graphics.getRawDeltaTime()*3f;
            if (Math.abs(this.current_x - this.target_x) < Settings.UI_SNAP_THRESHOLD) {
                this.current_x = this.target_x;
                this.hb.move(this.current_x, y);
            }
        }
        if (this.current_y != this.target_y) {
            this.current_y = Interpolation.linear.apply(current_y, target_y, NumberUtils.min(duration, 1f));
            duration += Gdx.graphics.getRawDeltaTime()*3f;
            if (Math.abs(this.current_y - this.target_y) < Settings.UI_SNAP_THRESHOLD) {
                this.current_y = this.target_y;
                this.hb.move(x, this.current_y);
            }
        }
        this.textColor.a = MathHelper.fadeLerpSnap(this.textColor.a, 1.0F);
        this.btnColor.a = this.textColor.a;
    }

    public void hideInstantly() {
        this.current_x = HIDE_X;
        this.target_x = HIDE_X;
        this.isHidden = true;
        this.textColor.a = 0.0F;
        this.btnColor.a = 0.0F;
    }

    public void hide() {
        this.isHidden = true;
    }

    public void show() {
        this.isHidden = false;
        this.textColor.a = 0.0F;
        this.btnColor.a = 0.0F;
        this.current_x = HIDE_X;
        this.target_x = x;
        this.current_y = -Settings.HEIGHT;
        this.target_y = y;
        this.hb.move(target_x, y);
    }

    public void render(SpriteBatch sb) {
        if (this.isHidden)
            return;
        renderButton(sb);
        if (FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 9999.0F, 0.0F) > 200.0F * Settings.scale) {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, msg, this.current_x, current_y, this.textColor, 0.8F);
        } else {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, msg, this.current_x, current_y, this.textColor);
        }
    }

    private void renderButton(SpriteBatch sb) {
        sb.setColor(this.btnColor);
        sb.draw(ImageMaster.REWARD_SCREEN_TAKE_BUTTON, this.current_x - H, current_y - 128.0F, H, 128.0F, W, H, Settings.scale, Settings.scale, 0.0F, 0, 0, W, H, false, false);
        if (this.hb.hovered && !this.hb.clickStarted) {
            //sb.setBlendFunction(Gdx.gl20.GL_SRC_ALPHA, Gdx.gl20.GL_DST_COLOR);
            sb.setColor(highColor);
            sb.draw(ImageMaster.REWARD_SCREEN_TAKE_BUTTON, this.current_x - H, current_y - 128.0F, H, 128.0F, W, H, Settings.scale, Settings.scale, 0.0F, 0, 0, W, H, false, false);
            //sb.setBlendFunction(Gdx.gl20.GL_SRC_ALPHA, Gdx.gl20.GL_ONE_MINUS_SRC_ALPHA);
            sb.setColor(Color.WHITE);

            onHoverRender(sb);
        }
        this.hb.render(sb);
    }

    protected void onHoverRender(SpriteBatch sb) {

    }
}



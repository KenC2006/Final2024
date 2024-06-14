package UI;

import Entities.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class PlayerUI extends UI {
    private final Player player;
    private BufferedImage intelligenceIcon;
    private BufferedImage killStreakIcon;
    private BufferedImage mainWeapon;
    private BufferedImage secondaryWeapon;
    private int barHeight = getPanelHeight() / 40;
    private int barWidth = getPanelWidth() / 5;
    private int brushStroke = getPanelWidth() / 400;
    private int textY;
    private int textX;
    private int boxHeight = brushStroke;
    private int hpFill = 0;
    private int manaFill = 0;
    private int currentPlayerHealth;
    private int currentPlayerMana;


    public PlayerUI(Player player) throws IOException {
        this.player = player;
        intelligenceIcon = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/intelligence.png")));
        killStreakIcon = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/skull.png")));
        intelligenceIcon = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/intelligence.png")));
        killStreakIcon = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/skull.png")));
    }

    public void resize() {
        barWidth = getPanelWidth() / 5;
        barHeight = getPanelHeight() / 40;
        intelligenceIcon = resizeImage(intelligenceIcon, barHeight, barHeight);
        killStreakIcon = resizeImage(killStreakIcon, barHeight, barHeight);
        brushStroke = getPanelWidth() / 400;
    }

    private void drawBar(double value, double maxValue, Color fillColor, Graphics2D g, int fill) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(brushStroke, boxHeight, barWidth, barHeight);
        g.setColor(fillColor);
        g.fillRect(brushStroke, boxHeight, (int) (value / maxValue * barWidth), barHeight);
        g.setColor(Color.RED);
        g.fillRect(brushStroke + (int) (value / maxValue * barWidth), boxHeight, fill, barHeight);
        g.setColor(Color.BLACK);
        String text = (int) value + "/" + (int) maxValue;
        Font font = new Font("Times New Roman", Font.BOLD, getPanelHeight() / 32);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        textX = brushStroke + (barWidth - metrics.stringWidth(text)) / 2;
        textY = boxHeight + (barHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(text, textX, textY);
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(brushStroke));
        g.drawRoundRect(brushStroke, boxHeight, barWidth, barHeight, brushStroke, brushStroke);
    }

    private void drawPlayerHP(Graphics2D g) {
        if (player.getStats().getHealth()<currentPlayerHealth && barWidth>0){
            double fill1=((double)currentPlayerHealth/player.getStats().getMaxHealth());
            double fill2=((double)player.getStats().getHealth()/player.getStats().getMaxHealth());
            hpFill=(int)((fill1-fill2)*barWidth)+hpFill;
            currentPlayerHealth=player.getStats().getHealth();

        }
        if (hpFill>0) hpFill--;
        drawBar(player.getStats().getHealth(), player.getStats().getMaxHealth(), Color.GREEN, g,hpFill);
    }

    private void drawPlayerMana(Graphics2D g) {
        if (player.getStats().getMana()<currentPlayerMana && barWidth>0){
            double fill1=((double)currentPlayerMana/player.getStats().getMaxMana());
            double fill2=((double)player.getStats().getMana()/player.getStats().getMaxMana());
            manaFill=(int)((fill1-fill2)*barWidth)+hpFill;
            currentPlayerMana=player.getStats().getMana();
        }
        if (manaFill > 0) manaFill -= 2;
        drawBar(player.getStats().getMana(), player.getStats().getMaxMana(), Color.CYAN, g, manaFill);
    }

    private void drawIntelligenceCount(Graphics2D g) {
        g.drawImage(intelligenceIcon, barWidth + brushStroke * 3, boxHeight, null);
        g.setColor(Color.CYAN);
        g.drawString(String.valueOf(player.getPlayerInventory().getIntelligence()), barWidth + barHeight + brushStroke * 5, textY);
        boxHeight += barHeight + brushStroke * 2;
    }

    private void drawKillStreakCount(Graphics2D g) {
        g.drawImage(killStreakIcon, barWidth + brushStroke * 3, boxHeight, null);
        g.setColor(Color.RED);
        g.drawString(String.valueOf(player.getKillStreak()), barWidth + barHeight + brushStroke * 5, textY);
        boxHeight += barHeight + brushStroke * 2;
    }

    private void drawWeaponSlot(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.drawRoundRect(brushStroke, boxHeight, barWidth / 2 - brushStroke, barHeight * 4, brushStroke, brushStroke);
        g.drawRoundRect(barWidth / 2 + brushStroke * 2, boxHeight, barWidth / 2 - brushStroke, barHeight * 4, brushStroke, brushStroke);
        boxHeight = brushStroke;
    }

    private BufferedImage resizeImage(BufferedImage image, int newH, int newW) {
        Image temp = image.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    @Override
    public void draw() {
        Graphics2D g = getGraphics();
        drawPlayerHP(g);
        drawIntelligenceCount(g);
        drawPlayerMana(g);
        drawKillStreakCount(g);
        drawWeaponSlot(g);
    }
}

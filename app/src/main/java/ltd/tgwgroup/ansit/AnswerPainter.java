package ltd.tgwgroup.ansit;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

/** The preview and real root surface use the same text fitting, padding and background alpha. */
final class AnswerPainter {
    static final String SAMPLE = "Answer preview\n\nThe answer to this screen capture will appear here.\n\nDrag the frame to move it or drag its corners to resize it. Adjust background transparency below.\n\nSettings are preserved and the saved layout will be used for future answers.";

    static void draw(Canvas canvas, String text, int width, int height, int screenWidth, int screenHeight, OverlayConfig config) {
        float scale = Math.min(screenWidth, screenHeight) / 1200f;
        float paddingX = 34 * scale, paddingY = 30 * scale;
        int textWidth = Math.max(1, (int)(width - paddingX * 2));
        int textHeight = Math.max(1, (int)(height - paddingY * 2));
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.DEFAULT); textPaint.setColor(Color.WHITE);
        String display = text.length() > 12000 ? text.substring(0, 12000) + "… (answer truncated)" : text;
        StaticLayout layout = null;
        for (int size = 40; size >= 28; size -= 2) {
            textPaint.setTextSize(size * scale);
            layout = StaticLayout.Builder.obtain(display, 0, display.length(), textPaint, textWidth)
                .setIncludePad(false).setLineSpacing(6 * scale, 1).build();
            if (layout.getHeight() <= textHeight) break;
        }
        if (layout.getHeight() > textHeight) {
            int maxLines = Math.max(1, (int)(textHeight / (textPaint.getFontSpacing() + 6 * scale)));
            layout = StaticLayout.Builder.obtain(display, 0, display.length(), textPaint, textWidth)
                .setIncludePad(false).setLineSpacing(6 * scale, 1).setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END).build();
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(34, 38, 48)); paint.setAlpha(config.backgroundAlpha());
        canvas.drawRoundRect(0, 0, width, height, 28 * scale, 28 * scale, paint);
        int saved = canvas.save();
        try {
            canvas.clipRect(0, 0, width, height);
            canvas.translate(paddingX, paddingY); layout.draw(canvas);
        } finally { canvas.restoreToCount(saved); }
    }
    private AnswerPainter() { }
}

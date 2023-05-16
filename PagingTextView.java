
import android.content.Context;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.List;

public class PagingTextView extends AppCompatTextView {
    private static final int DEFAULT_DELAY = 2000; // Delay in milliseconds
    private static final int DEFAULT_CHAR_PER_PAGE = 1; // Number of characters per page

    private Handler mHandler;
    private Runnable mShowPagingTextRunnable;
    private String mText;
    List<String> mPageStringList;
    int mPageStringIndex = 0;
    private int mDelay = DEFAULT_DELAY;
    private int mCharPerPage = DEFAULT_CHAR_PER_PAGE;

    public PagingTextView(Context context) {
        super(context);
        init();
    }

    public PagingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHandler = new Handler();
        mShowPagingTextRunnable = new Runnable() {
            @Override
            public void run() {
                showNextPage();
                mHandler.postDelayed(this, mDelay);
            }
        };
        initializePagingTextView();
    }

    public void setText(String text) {
        this.mText = text;
        mPageStringIndex = 0;
        mHandler.removeCallbacks(mShowPagingTextRunnable);
        if (mText.length() > mCharPerPage) {
            mPageStringList = split(mText, mCharPerPage);
            showNextPage();
            mHandler.postDelayed(mShowPagingTextRunnable, mDelay);
        } else {
            super.setText(mText);
        }
    }

    public void setDelay(int mDelay) {
        this.mDelay = mDelay;
    }

    private void showNextPage() {
        if (mPageStringIndex < mPageStringList.size()) {
            super.setText(mPageStringList.get(mPageStringIndex));
        }
        mPageStringIndex++;
        if (mPageStringIndex >= mPageStringList.size()) {
            mPageStringIndex = 0;
        }
    }

    public List<String> split(String text, int maxCharacters) {
        List<String> parts = new ArrayList<>();

        if (text.length() <= maxCharacters) {
            // If the text is shorter than or equal to the max characters, return the text as a single part
            parts.add(text);
            return parts;
        }

        int startIndex = 0;
        int endIndex = maxCharacters;

        while (startIndex < text.length()) {
            // Adjust the end index to cut at the last occurrence of a space character
            if (endIndex < text.length() && !Character.isWhitespace(text.charAt(endIndex))) {
                int lastSpaceIndex = text.lastIndexOf(" ", endIndex);
                if (lastSpaceIndex != -1 && lastSpaceIndex >= startIndex) {
                    endIndex = lastSpaceIndex;
                }
            }

            parts.add(text.substring(startIndex, endIndex).trim());

            startIndex = endIndex;
            endIndex += maxCharacters;

            if (endIndex >= text.length()) {
                // Add the remaining part of the text as the last part
                parts.add(text.substring(startIndex).trim());
                break;
            }
        }

        return parts;
    }


    public void calculateCharPerPage() {
        TextPaint textPaint = getPaint();
        float averageCharWidth = textPaint.measureText("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") / 52f;
        int maxCharsPerPage = (int) (getWidth() / averageCharWidth);

        String testText = "W"; // Test with a single character to check the width

        // Find the maximum number of characters that fit within the available width
        int charCount = 1;
        while (textPaint.measureText(testText) < getWidth()) {
            testText += "W"; // Add one more character
            charCount++;
        }

        //charPerPage = Math.min(charCount - 1, maxCharsPerPage);
        mCharPerPage = maxCharsPerPage;
    }

    public void initializePagingTextView() {
        // Wait until the view has been measured and laid out
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Calculate the maximum number of characters per page
                calculateCharPerPage();

                // Remove the global layout listener to prevent multiple calls
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mShowPagingTextRunnable);
    }
}

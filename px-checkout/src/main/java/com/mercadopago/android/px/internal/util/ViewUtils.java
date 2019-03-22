package com.mercadopago.android.px.internal.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.mercadolibre.android.ui.font.Font;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.view.MPEditText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import javax.annotation.Nonnull;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

import static android.view.View.GONE;

public final class ViewUtils {

    private ViewUtils() {
    }

    public static boolean shouldVisibleAnim(@NonNull final View viewToAnimate) {
        return hasEndedAnim(viewToAnimate) && viewToAnimate.getVisibility() != View.VISIBLE;
    }

    public static boolean shouldGoneAnim(@NonNull final View viewToAnimate) {
        return hasEndedAnim(viewToAnimate) && viewToAnimate.getVisibility() != View.GONE;
    }

    public static boolean hasEndedAnim(@NonNull final View viewToAnimate) {
        return viewToAnimate.getAnimation() == null ||
            (viewToAnimate.getAnimation() != null && viewToAnimate.getAnimation().hasEnded());
    }

    public static void addCancelToolbar(@NonNull final ViewGroup parentViewGroup, @ColorRes final int backgroundColor) {
        final Context context = parentViewGroup.getContext();
        final Toolbar toolbar = (Toolbar) ViewUtils.inflate(parentViewGroup, R.layout.px_toolbar_cancel);
        parentViewGroup.addView(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(context, backgroundColor));

        if (context instanceof AppCompatActivity) {
            final AppCompatActivity appCompatActivity = (AppCompatActivity) context;
            appCompatActivity.setSupportActionBar(toolbar);
            final ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> appCompatActivity.onBackPressed());
        }
    }

    public static void loadOrCallError(final String imgUrl, final ImageView logo, Callback callback) {
        if (!TextUtil.isEmpty(imgUrl)) {
            Picasso.with(logo.getContext())
                .load(imgUrl)
                .into(logo, callback);
        } else {
            callback.onError();
        }
    }

    public static void loadOrGone(@Nullable final CharSequence text, @NonNull final TextView textView) {
        if (TextUtil.isEmpty(text)) {
            textView.setVisibility(GONE);
        } else {
            textView.setText(text);
        }
    }

    public static void loadOrGone(@StringRes final int resId, @NonNull final TextView textView) {
        final CharSequence value = resId == 0 ? TextUtil.EMPTY : textView.getContext().getString(resId);
        loadOrGone(value, textView);
    }


    public static void loadOrGone(@DrawableRes final int resId, final ImageView imageView) {
        if(resId == 0){
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageResource(resId);
        }
    }

    public static void setMarginBottomInView(@NonNull final View view, final int marginBottom) {
        setMarginInView(view, 0, 0, 0, marginBottom);
    }

    public static void setMarginInView(@NonNull final View button, final int leftMargin, final int topMargin,
        final int rightMargin, final int bottomMargin) {
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        button.setLayoutParams(params);
    }

    public static void hideKeyboard(final Activity activity) {

        try {
            MPEditText editText = (MPEditText) activity.getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } catch (final Exception ex) {
        }
    }

    public static void openKeyboard(final View view) {
        view.requestFocus();
        final InputMethodManager imm =
            (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void showProgressLayout(final Activity activity) {
        showLayout(activity, true, false);
    }

    public static void showRegularLayout(final Activity activity) {
        showLayout(activity, false, true);
    }

    private static void showLayout(Activity activity, final boolean showProgress, final boolean showLayout) {

        final View form = activity.findViewById(R.id.mpsdkRegularLayout);
        final View progress = activity.findViewById(R.id.mpsdkProgressLayout);

        if (progress != null) {
            progress.setVisibility(showLayout ? GONE : View.VISIBLE);
        }

        if (form != null) {
            form.setVisibility(showProgress ? GONE : View.VISIBLE);
        }
    }

    public static void resizeViewGroupLayoutParams(final ViewGroup viewGroup, final int height, final int width) {
        final ViewGroup.LayoutParams params = viewGroup.getLayoutParams();
        final Context context = viewGroup.getContext();
        params.height = (int) context.getResources().getDimension(height);
        params.width = (int) context.getResources().getDimension(width);
        viewGroup.setLayoutParams(params);
    }

    public static void setColorInSpannable(final int color, final int indexStart, final int indexEnd,
        @NonNull final Spannable spannable) {
        if (color != 0) {
            spannable.setSpan(new ForegroundColorSpan(color), indexStart, indexEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    //TODO refactor
    public static void setFontInSpannable(final int indexStart, final int indexEnd,
        @NonNull final Spannable spannable, @Nullable final String fontStylePath, @NonNull final Context context) {

        if (TextUtil.isEmpty(fontStylePath)) {
            return;
        }

        final Typeface typeface = TypefaceUtils.load(context.getAssets(), fontStylePath);

        if (typeface == null) {
            return;
        }

        final StyleSpan styleSpan = new StyleSpan(typeface.getStyle());
        setFontInSpannable(indexStart, indexEnd, spannable, styleSpan);
    }

    //TODO refactor
    private static void setFontInSpannable(final int indexStart, final int indexEnd,
        @NonNull final Spannable spannable, @NonNull final StyleSpan styleSpan) {
        spannable.setSpan(styleSpan, indexStart, indexEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    //TODO refactor
    public static void setSemiBoldFontInSpannable(final int indexStart, final int indexEnd,
        @NonNull final Spannable spannable, @NonNull final Context context) {

        if (TextUtil.isEmpty(Font.SEMI_BOLD.getFontPath())) {
            setFontInSpannable(indexStart, indexEnd, spannable, new StyleSpan(Typeface.BOLD));
        } else {
            final Typeface semiBold = TypefaceUtils.load(context.getAssets(), Font.SEMI_BOLD.getFontPath());
            if (semiBold == null) {
                setFontInSpannable(indexStart, indexEnd, spannable, new StyleSpan(Typeface.BOLD));
                return;
            }
            setFontInSpannable(indexStart, indexEnd, spannable, new StyleSpan(semiBold.getStyle()));
        }
    }

    public static void stretchHeight(@NonNull final View view) {
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        );
        view.setLayoutParams(params);
    }

    @Nonnull
    public static View inflate(@Nonnull final ViewGroup parent, @LayoutRes final int layout) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    @Nonnull
    public static View compose(@Nonnull final ViewGroup container, @Nonnull final View... children) {
        for (View child : children) {
            container.addView(child);
        }
        return container;
    }

    @Nonnull
    public static View compose(@Nonnull final ViewGroup container, @Nonnull final View child) {
        container.addView(child);
        return container;
    }

    @Nonnull
    public static LinearLayout createLinearContainer(final Context context) {
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        return linearLayout;
    }

    @Nonnull
    public static ScrollView createScrollContainer(final Context context) {
        final ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(scrollView
            .getContext()
            .getResources()
            .getColor(R.color.px_white_background));
        return scrollView;
    }

    public static void cancelAnimation(@NonNull final View targetView) {
        final Animation animation = targetView.getAnimation();
        if (animation != null) {
            animation.cancel();
        }
    }

}

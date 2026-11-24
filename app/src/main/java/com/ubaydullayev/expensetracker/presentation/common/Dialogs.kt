package com.ubaydullayev.expensetracker.presentation.common

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.DialogTitleBinding

/**
 * Finara dialoglari uchun yordamchilar. Uslubning o'zi mavzuda (dialog_styles.xml),
 * bu yerda faqat mavzu bilan qilib bo'lmaydigan ikki narsa:
 * ikonkali sarlavha va xavfli amal tugmasini qizilga bo'yash.
 */
enum class DialogTone(@ColorRes val tile: Int, @ColorRes val icon: Int) {
    DEFAULT(R.color.fin_blue_tint, R.color.fin_blue),
    WARNING(R.color.fin_orange_tint, R.color.fin_orange_text),
    DANGER(R.color.fin_error_tint, R.color.fin_error)
}

/** Sarlavha o'rniga: rangli ikonka plitkasi + navy sarlavha. */
fun MaterialAlertDialogBuilder.setFinaraTitle(
    title: CharSequence,
    @DrawableRes icon: Int,
    tone: DialogTone = DialogTone.DEFAULT
): MaterialAlertDialogBuilder {
    val ctx = context
    val header = DialogTitleBinding.inflate(LayoutInflater.from(ctx))
    header.tvTitle.text = title
    header.ivIcon.setImageResource(icon)
    header.ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, tone.icon))
    header.iconTile.background = GradientDrawable().apply {
        cornerRadius = 14f * ctx.resources.displayMetrics.density
        setColor(ContextCompat.getColor(ctx, tone.tile))
    }
    return setCustomTitle(header.root)
}

fun MaterialAlertDialogBuilder.setFinaraTitle(
    titleRes: Int,
    @DrawableRes icon: Int,
    tone: DialogTone = DialogTone.DEFAULT
): MaterialAlertDialogBuilder = setFinaraTitle(context.getString(titleRes), icon, tone)

/**
 * Xavfli amal (o'chirish va h.k.): asosiy tugma qizil. Dialog ko'rsatilgandan keyin chaqiriladi,
 * chunki tugmalar faqat show() dan keyin yaratiladi.
 */
fun AlertDialog.tintPositiveDanger(): AlertDialog {
    getButton(DialogInterface.BUTTON_POSITIVE)?.backgroundTintList =
        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fin_error))
    return this
}

/**
 * Tugmalarni pastda teng kenglikda joylaydi (Cancel | Asosiy amal).
 * Standart AlertDialog'da ular o'ngda, oralarida bo'sh joy (Space) bo'ladi.
 * show() dan keyin chaqiriladi.
 */
fun AlertDialog.stretchButtons(): AlertDialog {
    val buttons = listOf(
        getButton(DialogInterface.BUTTON_NEUTRAL),
        getButton(DialogInterface.BUTTON_NEGATIVE),
        getButton(DialogInterface.BUTTON_POSITIVE)
    ).filter { it != null && it.visibility == View.VISIBLE }
    val bar = buttons.firstOrNull()?.parent as? LinearLayout ?: return this

    for (i in 0 until bar.childCount) {
        val child = bar.getChildAt(i)
        if (child is Space) child.visibility = View.GONE
    }
    bar.orientation = LinearLayout.HORIZONTAL
    buttons.forEachIndexed { index, button ->
        val lp = button.layoutParams as? LinearLayout.LayoutParams ?: return@forEachIndexed
        lp.width = 0
        lp.weight = 1f
        lp.marginStart = if (index == 0) 0 else (8 * context.resources.displayMetrics.density).toInt()
        lp.marginEnd = 0
        button.layoutParams = lp
    }
    return this
}

/** Builder'dan to'g'ridan-to'g'ri: create + show + teng tugmalar. */
fun MaterialAlertDialogBuilder.showFinara(): AlertDialog = show().stretchButtons()

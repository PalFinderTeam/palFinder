package com.github.palFinderTeam.palfinder.utils


import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.getSystemService
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.login.LoginActivity

/**
 * create a popUp windows
 * @param context : current context
 * @param textId : id of the string of the main text. Base is no_account_warning
 * @param continueButtonTextId : id of the string of the continue button. Base is continue.
 * @param cancelButtonTextId : id of the string of the cancel button. Base is cancel
 * @param function: function to apply when continue is pressed
 */
fun createPopUp(
    context: Context,
    textId: Int = R.string.no_account_warning,
    continueButtonTextId: Int = R.string.continu,
    cancelButtonTextId: Int = R.string.cancel,
    function: () -> Unit,
) {

    val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_window, context.getSystemService())
    val popUpWindow = PopupWindow(
        view,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )


    val buttonContinue = view.findViewById<TextView>(R.id.continue_warning_button)
    val buttonCancel = view.findViewById<Button>(R.id.cancel_warning_button)
    val textView = view.findViewById<TextView>(R.id.warning_text_view)

    textView.text = context.getString(textId)
    buttonContinue.text = context.getString(continueButtonTextId)
    buttonCancel.text = context.getString(cancelButtonTextId)

    buttonCancel.setOnClickListener {
        popUpWindow.dismiss()
    }

    buttonContinue.setOnClickListener {
        popUpWindow.dismiss()
        function()
    }

    popUpWindow.showAtLocation(
        view,
        Gravity.CENTER,
        0,
        0
    )

    popUpWindow.dimBehind()

}

/**
 * dim the background of a popupWindow, code taken from : https://stackoverflow.com/questions/35874001/dim-the-background-using-popupwindow-in-android
 */
private fun PopupWindow.dimBehind() {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = 0.3f
    wm.updateViewLayout(container, p)
}

/**
 * Create a popup which redirect to the login screen, used when performing
 * a action requiring an account when in no-account mode.
 *
 * @param context Context used for the popup and start the activity
 * @param textId Id to the string resource of the popup message
 */
fun createNoAccountPopUp(context: Context, textId: Int) {
    createPopUp(
        context,
        textId = textId,
        continueButtonTextId = R.string.login
    )
    {
        startActivity(context, Intent(context, LoginActivity::class.java), null)
    }
}

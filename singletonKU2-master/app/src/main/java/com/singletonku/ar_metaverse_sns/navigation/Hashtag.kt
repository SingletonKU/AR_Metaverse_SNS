package com.singletonku.ar_metaverse_sns.navigation

import android.content.Context
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

class Hashtag constructor(ctx : Context) : ClickableSpan() {

    interface HashtagClickEventListener{
        fun onHashtagClickEvent(data : String, context: Context)
    }

    var mHashtagClickEventListener : HashtagClickEventListener? = null
    var context : Context? = null
    var textPaint : TextPaint? = null

    init {
        context = ctx
    }

    fun setOnHashtagClickEventListener(listener: HashtagClickEventListener) {
        mHashtagClickEventListener = listener
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        textPaint = ds
        ds.setColor(ds.linkColor)
        ds.setARGB(255, 30, 144, 255)

    }

    override fun onClick(widget: View) {
        var tv : TextView = widget as TextView
        var s : Spanned = tv.text as Spanned
        var start : Int = s.getSpanStart(this)
        var end : Int = s.getSpanEnd(this)
        var theWord : String = s.subSequence(start+1, end).toString()
        mHashtagClickEventListener?.onHashtagClickEvent(theWord, context!!)
    }
}
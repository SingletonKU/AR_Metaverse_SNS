package com.singletonku.ar_metaverse_sns

import android.os.SystemClock
import android.view.View

public abstract class OnSingleClickListener : View.OnClickListener {

    private final var MIN_CLICK_INTERVAL : Long = 3000
    private var mLastClickTime : Long = 0

    public abstract fun onSingleClick (v : View)


    override fun onClick(p0: View?) {
        var currentClickTime = SystemClock.uptimeMillis()
        var elapsedTime = currentClickTime - mLastClickTime
        mLastClickTime = currentClickTime

        //중복클릭이 아닐경우
        if(elapsedTime > MIN_CLICK_INTERVAL){
            onSingleClick(p0!!)
        }

    }

}
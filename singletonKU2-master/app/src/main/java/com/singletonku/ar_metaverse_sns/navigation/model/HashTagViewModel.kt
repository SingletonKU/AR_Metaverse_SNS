package com.singletonku.ar_metaverse_sns.navigation.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HashTagViewModel : ViewModel(){
    val selectedHashtag = MutableLiveData<String>()

    fun setLiveData(hashtagString: String){
        selectedHashtag.value = hashtagString
    }

}
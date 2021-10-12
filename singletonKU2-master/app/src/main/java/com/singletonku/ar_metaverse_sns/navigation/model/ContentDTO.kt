package com.singletonku.ar_metaverse_sns.navigation.model

import android.text.SpannableString

data class ContentDTO(
    var hashtagList : ArrayList<String> = arrayListOf<String>(),
    //var hashtag : MutableMap<String, Boolean> = HashMap(),
    var explain : String? = null,
    var imageUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timestamp : Long? = null,
    var favoriteCount : Int = 0,
    var favorites : MutableMap<String, Boolean> = HashMap()){

    data class Comment(
        var uid : String? = null,
        var userId : String? = null,
        var comment: String? = null,
        var timestamp: Long? = null)
}


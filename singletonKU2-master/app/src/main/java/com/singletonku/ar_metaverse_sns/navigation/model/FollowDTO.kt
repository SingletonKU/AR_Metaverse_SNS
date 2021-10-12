package com.singletonku.ar_metaverse_sns.navigation.model

data class FollowDTO(
    var followerCount : Int = 0,
    //중복 팔로워 방지
    var followers : MutableMap<String, Boolean> = HashMap(),
    var followingCount : Int = 0,
    var followings : MutableMap<String, Boolean> = HashMap()
)
package com.andrio_kt_dev.billboard.model

data class AdFilter(
    val time: String? = null,
    val cat_time: String? = null,

    val cat_country_send_time: String? = null,
    val cat_country_city_send_time: String? = null,
    val cat_country_city_index_send_time: String? = null,
    val cat_index_send_time: String? = null,
    val cat_send_time: String? = null,

    val country_send_time: String? = null,
    val country_city_send_time: String? = null,
    val country_city_index_send_time: String? = null,
    val index_send_time: String? = null,
    val send_time: String? = null
)

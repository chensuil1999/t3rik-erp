package com.t3rik.mobile.mes.service

import com.t3rik.mes.md.domain.MdItem

/**
 * @Author: Wywzyy
 * @Date: 2024-11-04-16:23
 * @Description:
 */
interface IMdItemMobileService {
    fun getMdItemDetailById(mdItemId: Long): MdItem
}
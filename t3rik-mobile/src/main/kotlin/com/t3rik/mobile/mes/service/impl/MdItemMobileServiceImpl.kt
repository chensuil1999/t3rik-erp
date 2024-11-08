package com.t3rik.mobile.mes.service.impl

import com.t3rik.mes.md.domain.MdItem
import com.t3rik.mes.md.service.IMdItemService
import com.t3rik.mobile.mes.service.IMdItemMobileService
import jakarta.annotation.Resource
import org.springframework.stereotype.Service

/**
 * @Author: Wywzyy
 * @Date: 2024-11-04-16:23
 * @Description:
 */
@Service
class MdItemMobileServiceImpl : IMdItemMobileService {
    @Resource
    lateinit var mdItemMobService: IMdItemService
    override fun getMdItemDetailById(mdItemId: Long): MdItem {
        TODO("Not yet implemented")
        println(mdItemId)
        return this.mdItemMobService.lambdaQuery()
                .eq(MdItem::getItemId, mdItemId).one()
    }
}
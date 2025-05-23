package com.t3rik.mes.md.controller;

import cn.hutool.core.lang.Assert;
import com.t3rik.common.annotation.Log;
import com.t3rik.common.constant.MsgConstants;
import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.core.controller.BaseController;
import com.t3rik.common.core.domain.AjaxResult;
import com.t3rik.common.core.domain.entity.ItemType;
import com.t3rik.common.core.page.TableDataInfo;
import com.t3rik.common.enums.BusinessType;
import com.t3rik.common.enums.EnableFlagEnum;
import com.t3rik.common.exception.BusinessException;
import com.t3rik.common.support.ItemTypeSupport;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.mes.aspect.BarcodeGen;
import com.t3rik.mes.md.domain.MdItem;
import com.t3rik.mes.md.domain.MdProductBom;
import com.t3rik.mes.md.service.IItemTypeService;
import com.t3rik.mes.md.service.IMdItemService;
import com.t3rik.mes.md.service.IMdProductBomService;
import com.t3rik.mes.wm.utils.WmBarCodeUtil;
import com.t3rik.system.strategy.AutoCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/mes/md/mditem")
public class MdItemController extends BaseController {

    @Autowired
    private IMdItemService mdItemService;

    @Autowired
    private IItemTypeService iItemTypeService;

    @Autowired
    private WmBarCodeUtil barcodeUtil;

    @Autowired
    private IMdProductBomService mdProductBomService;

    @Autowired
    private AutoCodeUtil autoCodeUtil;
    /**
     * 列表查询
     *
     * @param mdItem
     * @return
     */
    @GetMapping("/list")
    public TableDataInfo list(MdItem mdItem) {
        startPage();
        List<MdItem> list = mdItemService.selectMdItemList(mdItem);
        return getDataTable(list);
    }
    //生产任务单需要的物料数据
    @GetMapping("/queryForTask/{itemId}")
    public TableDataInfo listForTask(@PathVariable("itemId") Long itemId) {
        MdProductBom mdbom = new MdProductBom();
        mdbom.setItemId(itemId);
        List<MdProductBom> listItemBom = mdProductBomService.selectMdProductBomList(mdbom);
        //System.out.println("ooo" + listItemBom);
//        Long[] ids = listItemBom.
        // 将List中的age属性列转为int数组
        long[]  ids = listItemBom.stream()
                .mapToLong(MdProductBom::getBomItemId)
                .toArray();
        // 使用Stream API将long[]转换为Long[]
        Long[] lids = Arrays.stream(ids)
                .boxed()
                .toArray(Long[]::new);
        List<MdItem> listItem = mdItemService.lambdaQuery().in(MdItem::getItemId, lids).list();
        startPage();
        return getDataTable(listItem);
    }


    /**
     * 主键查询
     *
     * @param itemId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:md:mditem:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId) {
        return AjaxResult.success(mdItemService.selectMdItemById(itemId));
    }

    /**
     * 新增
     *
     * @param mdItem
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:md:mditem:add')")
    @Log(title = "物料管理", businessType = BusinessType.INSERT)
    //@BarcodeGen(barcodeType = UserConstants.BARCODE_TYPE_ITEM)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MdItem mdItem) {
        if (UserConstants.NOT_UNIQUE.equals(mdItemService.checkItemCodeUnique(mdItem))) {
            return AjaxResult.error("新增物料" + mdItem.getItemCode() + "失败，物料编码已存在");
        }

        //此处删掉同名判断函数，此注释仅作标记。
        ItemType type = iItemTypeService.selectItemTypeById(mdItem.getItemTypeId());
        if (StringUtils.isNotNull(type)) {
            mdItem.setItemTypeCode(type.getItemTypeCode());
            mdItem.setItemTypeName(type.getItemTypeName());
            mdItem.setItemOrProduct(type.getItemOrProduct());
        }
        mdItem.setCreateBy(getUsername());
        if(mdItem.getItemOrProduct().equals("ITEM")) {
            autoCodeUtil.saveSerialCode("ITEM_CODE", null);
        } else {
            autoCodeUtil.saveSerialCode("PRODUCT_CODE", null);
        }
        mdItemService.insertMdItem(mdItem);
        //barcodeUtil.generateBarCode(UserConstants.BARCODE_TYPE_ITEM, mdItem.getItemId(), mdItem.getItemCode(), mdItem.getItemName());
        return AjaxResult.success(mdItem.getItemId());
    }

    /**
     * 列表查询-只查询产品列表
     *
     * @param mdItem
     * @return
     */
    @Log(title = "物料管理", businessType = BusinessType.SEARCH)
    @GetMapping("/list/product/{type}")
    public TableDataInfo listProduct(MdItem mdItem, @PathVariable("type") String type) {
        startPage();
        // 根据类型查询
        String itemTypeId = ItemTypeSupport.getDefaultDataIdByItemType(type);
        //System.out.println("dddddddd:" + type);
        Assert.notNull(itemTypeId, () -> new BusinessException(MsgConstants.PARAM_ERROR));
        mdItem.setItemTypeId(Long.valueOf(itemTypeId));
        mdItem.setEnableFlag(EnableFlagEnum.YES.getCode());
        List<MdItem> list = mdItemService.selectMdItemList(mdItem);
        return getDataTable(list);
    }

    /**
     * 新增-只新增产品品类,这个似乎没有运行到。
     */
    @PreAuthorize("@ss.hasPermi('mes:md:mditem:add')")
    @Log(title = "物料管理", businessType = BusinessType.INSERT)
    @BarcodeGen(barcodeType = UserConstants.BARCODE_TYPE_ITEM)
    @PostMapping("/product/{type}")
    public AjaxResult addProduct(@Validated @RequestBody MdItem mdItem, @PathVariable("type") String type) {
        System.out.println("yes it me");
        if (UserConstants.NOT_UNIQUE.equals(mdItemService.checkItemCodeUnique(mdItem))) {
            return AjaxResult.error("新增物料" + mdItem.getItemCode() + "失败，物料编码已存在");
        }
        this.mdItemService.addItemOrProduct(mdItem, type);
        return AjaxResult.success(mdItem.getItemId());
    }

    /**
     * 更新
     *
     * @param mdItem
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:md:mditem:edit')")
    @Log(title = "物料管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MdItem mdItem) {
        if (UserConstants.NOT_UNIQUE.equals(mdItemService.checkItemCodeUnique(mdItem))) {
            return AjaxResult.error("新增物料" + mdItem.getItemCode() + "失败，物料编码已存在");
        }
//        if(StringUtils.isNull(mdItem)) {
//            return AjaxResult.error("更新物料出错");
//        }
        ItemType type = iItemTypeService.selectItemTypeById(mdItem.getItemTypeId());
        if (StringUtils.isNotNull(type)) {
            mdItem.setItemTypeCode(type.getItemTypeCode());
            mdItem.setItemTypeName(type.getItemTypeName());
            mdItem.setItemOrProduct(type.getItemOrProduct());
        }
        if (StringUtils.isNotNull(mdItem.getSafeStockFlag()) && "N".equals(mdItem.getSafeStockFlag())) {
            mdItem.setMinStock(0D);
            mdItem.setMaxStock(0D);
        }
        mdItem.setUpdateBy(getUsername());
        return toAjax(mdItemService.updateMdItem(mdItem));
    }

    @PreAuthorize("@ss.hasPermi('mes:md:mditem:remove')")
    @Log(title = "物料管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds) {
        return toAjax(mdItemService.deleteByItemIds(itemIds));
    }


}

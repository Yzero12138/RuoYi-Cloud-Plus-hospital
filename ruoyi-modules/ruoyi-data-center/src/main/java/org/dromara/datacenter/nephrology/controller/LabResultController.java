package org.dromara.datacenter.nephrology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.datacenter.nephrology.domain.bo.LabResultBo;
import org.dromara.datacenter.nephrology.domain.vo.LabResultVo;
import org.dromara.datacenter.nephrology.service.ILabResultService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lab result detail controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/nephrology/lab-result")
public class LabResultController extends BaseController {

    private final ILabResultService labResultService;

    /**
     * List lab results.
     */
    @SaCheckPermission("data-center:nephrology:lab-result:list")
    @GetMapping("/list")
    public TableDataInfo<LabResultVo> list(LabResultBo bo, PageQuery pageQuery) {
        return labResultService.selectPageLabResultList(bo, pageQuery);
    }

    /**
     * Get result detail.
     */
    @SaCheckPermission("data-center:nephrology:lab-result:query")
    @GetMapping("/{id}")
    public R<LabResultVo> getInfo(@PathVariable Long id) {
        return R.ok(labResultService.selectLabResultById(id));
    }
}

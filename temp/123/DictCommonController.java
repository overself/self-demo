package com.jjl.dxz.module.biz.workflow.business.controller;

import com.google.common.collect.Lists;
import com.jjl.dxz.framework.common.util.CollUtil;
import com.jjl.dxz.module.abroad.service.base.AbroadDictBo;
import com.jjl.dxz.module.abroad.service.search.AbroadDictSearchService;
import com.jjl.dxz.module.abroad.service.search.UniversityCollegeDepartmentSearchService;
import com.jjl.dxz.module.abroad.service.search.UniversitySearchService;
import com.jjl.dxz.module.biz.workflow.base.dto.DictItemPutDownBatchDto;
import com.jjl.dxz.module.biz.workflow.base.dto.DictItemPutDownDto;
import com.jjl.dxz.module.biz.workflow.base.dto.GradeSearchDto;
import com.jjl.dxz.module.biz.workflow.base.service.DictItemService;
import com.jjl.dxz.module.biz.workflow.base.vo.CountryCodeVo;
import com.jjl.dxz.module.biz.workflow.base.vo.CountryVo;
import com.jjl.dxz.module.biz.workflow.base.vo.DictItemPutDownVo;
import com.jjl.dxz.module.system.service.cache.CountryCodeCachedService;
import com.jjl.features.common.dto.NameValuePair;
import com.jjl.features.enums.nsystem.CountryCodeNoEnum;
import com.jjl.features.web.model.ResultResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 公共接口 CTL
 *
 * @author HanWenjie
 */
@RestController
@RequestMapping("/api/wf/p/common")
public class DictCommonController {

    @DubboReference
    private DictItemService dictItemService;
    @DubboReference
    private AbroadDictSearchService abroadDictSearchService;
    @DubboReference
    private UniversitySearchService universitySearchService;
    @DubboReference
    private UniversityCollegeDepartmentSearchService universityCollegeDepartmentSearchService;
    @DubboReference
    private CountryCodeCachedService countryCodeCachedService;

    /**
     * 查询常量项目下拉选项
     *
     * @param putDownDto 常量项目下拉选项条件
     * @return 常量项目下拉选项列表
     */
    @PostMapping("/dict/put-down")
    public ResultResponse<List<DictItemPutDownVo>> getDictItemPage(@RequestBody @Validated DictItemPutDownDto putDownDto) {
        return dictItemService.selectDictPutDown(putDownDto);
    }

    /**
     * 查询常量项目下拉选项(可简单批量，可综合批量)
     *
     * @param batchDto 常量项目下拉选项条件
     *        typeCodes：但指定字典类型，全量查询  List<String>
     *        typeList：可综合查询字典类型  List<DictItemPutDownDto>
     * @return 常量项目下拉选项列表
     */
    @PostMapping("/dict/batch/put-down")
    public ResultResponse<Map<String,List<DictItemPutDownVo>>> getDictItemPage(@RequestBody @Validated DictItemPutDownBatchDto batchDto) {
        return dictItemService.selectDictPutDownBatch(batchDto);
    }

    /**
     * 国家查询
     *
     * @return List<CountryUniversityDeptVo>
     */
    @PostMapping("/country-list")
    public List<CountryVo> getCountryList() {
        List<CountryVo> result = Lists.newArrayList();
        List<AbroadDictBo>  abroadDictBos = abroadDictSearchService.getGradeByParentId("0",1);
        if(CollUtil.isNotEmpty(abroadDictBos)){
            abroadDictBos.stream().forEach(i->{result.add(CountryVo.builder().code(i.getDictValue()).name(i.getDescription()).build());});
        }
        return result;
    }

    /**
     * 学段查询
     *
     * @param dto
     * @return List<CountryUniversityDeptVo>
     */
    @PostMapping("/grade-list")
    public List<CountryVo> getGradeList(@RequestBody @Validated GradeSearchDto dto) {
        List<CountryVo> result = Lists.newArrayList();
        List<AbroadDictBo>  abroadDictBos = abroadDictSearchService.getGradeByParentId(dto.getCode(),4);
        if(CollUtil.isNotEmpty(abroadDictBos)){
            abroadDictBos.stream().forEach(i->{result.add(CountryVo.builder().code(i.getDictKey()).name(i.getDictValue()).build());});
        }
        return result;
    }

    /**
     * 国际区号查询
     *
     * @return List<String>
     */
    @PostMapping("/country_tel_code")
    public List<CountryCodeVo> getCountryTelCodeList() {
        List<CountryCodeVo> list = Lists.newArrayList();
        List<NameValuePair> nameValuePairs = CountryCodeNoEnum.toList();
        if (CollUtil.isNotEmpty(nameValuePairs)) {
            nameValuePairs.forEach(data -> {
                CountryCodeVo countryCodeVo = new CountryCodeVo();
                countryCodeVo.setTelCode("+" + data.getValue());
                countryCodeVo.setNameZh(data.getName());
                list.add(countryCodeVo);
            });
        }
//        Response<List<CountryCodeModel>> response = countryCodeCachedService.countryList();
//        if(response.isSuccess() && CollUtil.isNotEmpty(response.getResult())){
//            return BeanUtil.toBean(response.getResult(),CountryCodeVo.class);
//        }
        return list;
    }
}

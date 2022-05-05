package com.hanwj.sample.web.model.user.dto;

import com.hanwj.core.validator.check.CodeRange;
import com.hanwj.core.validator.check.EnumRange;
import com.hanwj.sample.web.model.user.enums.TenantStatusEnum;
import com.hanwj.web.model.dto.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 租户信息
 *
 * @author HanWenjie
 */
@Data
@ApiModel(value = "Demo DTO 对象")
public class TenantDto extends BaseDto {

    private static final long serialVersionUID = 1L;
    /**
     * 数据ID
     */
    @ApiModelProperty(value = "数据ID")
    private String id;
    /**
     * 电话
     */
    @Pattern(message = "电话", regexp = "((^(13|15|18)[0-9]{9}$)|(^0[1,2]{1}\\d{1}-?\\d{8}$)|(^0[3-9] {1}\\d{2}-?\\d{7,8}$)|(^0[1,2]{1}\\d{1}-?\\d{8}-(\\d{1,4})$)|(^0[3-9]{1}\\d{2}-? \\d{7,8}-(\\d{1,4})$))")
    @ApiModelProperty(value = "电话")
    private String phone;
    /**
     * 租户名称
     */
    @ApiModelProperty(value = "租户名称")
    private String name;
    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;
    /**
     * 账号编码
     */
    @ApiModelProperty(value = "账号编码")
    private String accountCode;
    /**
     * 账号编码逻辑字段
     */
    //@ApiModelProperty(value = "账号编码逻辑字段")
    //private String account;

    /**
     * 租户编号
     */
    @NotNull(message = "{title.tenantCode}")
    @ApiModelProperty(value = "租户编号")
    private String code;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private LocalDate startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private LocalDate endTime;
    /**
     * 租户冻结标记,9:冻结,0:正常
     */
    //@CodeRange(value = {"0","9"},  message = "{title.codeRange}", required = true)
    @EnumRange(target = TenantStatusEnum.class, message = "{title.status}")
    @ApiModelProperty(value = "租户冻结标记,9:冻结,0:正常")
    private TenantStatusEnum status = TenantStatusEnum.fromValue("0");

    /**  以下为API注解校验测试字段 */
    /**
     * 测试最小值
     */
    @Min(value = 10, message = "测试最小值")
    @ApiModelProperty(value = "测试最小值：10")
    private Integer rangeMin = 10;
    /**
     * 测试范围值
     */
    @Range(min = 1, max = 3, message = "{title.range}")
    @ApiModelProperty(value = "测试范围值：1~3")
    private int rangeMinMax = 1;
    /**
     * 字符码表范围
     */
    @CodeRange(value = {"01", "02"}, message = "{title.codeRange}")
    @ApiModelProperty(value = "字符码表范围: 01，02")
    private String strRangeCode = "01";

    /**
     *
     */
    @CodeRange(value = {"1", "2"}, message = "{title.codeRange}")
    @ApiModelProperty(value = "数值码表范围：1，2")
    private Integer intRangeCode = 1;


}

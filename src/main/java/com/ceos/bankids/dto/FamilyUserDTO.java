package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class FamilyUserDTO {

    @ApiModelProperty(example = "주어랑")
    private String username;
    @ApiModelProperty(example = "true")
    private Boolean isFemale;
    @ApiModelProperty(example = "true")
    private Boolean isKid;


    public FamilyUserDTO(User user) {
        this.username = user.getUsername();
        this.isFemale = user.getIsFemale();
        this.isKid = user.getIsKid();
    }
}

package com.coolchat.domain.vo;

import com.coolchat.domain.po.GroupInfo;
import com.coolchat.domain.po.UserContact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoVO implements Serializable {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;

}

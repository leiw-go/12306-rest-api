package com.sinosun.train.model.response;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.MoreObjects;

/**
 * Created on 2019/1/10 20:37.
 *
 * @author caogu
 */
@JSONType(naming = PropertyNamingStrategy.PascalCase)
public class SecondClassTicketListResult extends BaseResult {
    private SecondClassTicketList result;

    public SecondClassTicketListResult() {
    }

    public SecondClassTicketListResult(SecondClassTicketList result) {
        this.result = result;
    }

    public SecondClassTicketList getResult() {
        return result;
    }

    public void setResult(SecondClassTicketList result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("result", result)
                .toString();
    }
}

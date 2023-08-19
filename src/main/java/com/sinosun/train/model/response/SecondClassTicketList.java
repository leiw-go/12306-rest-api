package com.sinosun.train.model.response;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.MoreObjects;

import java.util.List;

/**
 * Created on 2019/1/10 20:45.
 *
 * @author caogu
 */
@JSONType(naming = PropertyNamingStrategy.PascalCase)
public class SecondClassTicketList {
    protected List<SecondClassTicket> tickets;

    public List<SecondClassTicket> getTickets() {
        return tickets;
    }

    public void setTickets(List<SecondClassTicket> tickets) {
        this.tickets = tickets;
    }

    public SecondClassTicketList(List<SecondClassTicket> tickets) {
        this.tickets = tickets;
    }

    public SecondClassTicketList() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tickets", tickets)
                .toString();
    }
}

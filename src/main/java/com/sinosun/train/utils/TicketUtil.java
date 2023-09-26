package com.sinosun.train.utils;

import com.sinosun.train.model.request.GetTicketListRequest;
import com.sinosun.train.model.response.SecondClassTicket;
import com.sinosun.train.model.response.Ticket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TicketUtil {
    public static List<Ticket> filterTicketTypeAndNum(GetTicketListRequest requestBody, List<Ticket> tickets) throws InterruptedException {
        return tickets.stream().filter(f -> requestBody.getTrainType().contains(f.getTrainType()))
                .filter(f -> !"0".equals(f.getEdzNum()))
                .collect(Collectors.toList());
    }

    public static List<SecondClassTicket> filterRealWeWantLineAndBuildTickets(List<Ticket> tickets, Map.Entry<String, List<String>> entry) throws InterruptedException {
        return tickets.stream()
                .filter(f -> entry.getValue().contains(f.getTrainCode()))
                .map(TicketUtil::transferTicket).collect(Collectors.toList());
    }

    private static SecondClassTicket transferTicket(Ticket ticket) {
        SecondClassTicket secondClassTicket = new SecondClassTicket();
        BeanUtils.copyProperties(ticket, secondClassTicket);
        secondClassTicket.setSecondClassTicKetNum(ticket.getEdzNum());
        secondClassTicket.setNoSeatTicketNum(ticket.getWzNum());
        secondClassTicket.setOtherCheapTicketNum(ticket.getQtNum());
        return secondClassTicket;
    }
}

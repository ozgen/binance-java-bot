package com.ozgen.binancebot.model.events;

import com.ozgen.binancebot.model.bot.BuyOrder;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@ToString
public class NewSellOrderEvent extends ApplicationEvent {
    private final BuyOrder buyOrder;

    public NewSellOrderEvent(Object source, BuyOrder buyOrder) {
        super(source);
        this.buyOrder = buyOrder;
    }

    public BuyOrder getBuyOrder() {
        return buyOrder;
    }
}

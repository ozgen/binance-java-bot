package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.adapters.binance.BinanceAPI;
import com.ozgen.binancebot.model.binance.AssetBalance;
import com.ozgen.binancebot.model.binance.CancelAndNewOrderResponse;
import com.ozgen.binancebot.model.binance.IntervalType;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.model.binance.OpenOrder;
import com.ozgen.binancebot.model.binance.OrderInfo;
import com.ozgen.binancebot.model.binance.OrderResponse;
import com.ozgen.binancebot.model.binance.TickerData;
import com.ozgen.binancebot.service.AssetBalanceService;
import com.ozgen.binancebot.service.BinanceOrderService;
import com.ozgen.binancebot.service.KlineDataService;
import com.ozgen.binancebot.service.TickerDataService;
import com.ozgen.binancebot.utils.parser.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceApiManager {

    private final BinanceAPI binanceAPI;
    private final TickerDataService tickerDataService;
    private final BinanceOrderService binanceOrderService;
    private final AssetBalanceService assetBalanceService;
    private final KlineDataService klineDataService;


    public TickerData getTickerPrice24(String symbol) throws Exception {
        String tickerPrice24 = this.binanceAPI.getTickerPrice24(symbol);
        TickerData tickerData = JsonParser.parseTickerJson(tickerPrice24);

        log.info("'{}' of symbol ticker data is parsed, successfully.", symbol);
        return this.tickerDataService.createTickerData(tickerData);
    }

    public boolean checkSymbol(String symbol){
        return this.binanceAPI.checkSymbol(symbol);
    }

    List<OrderInfo> getOpenOrders(String symbol) throws Exception {
        String openOrdersJson = this.binanceAPI.getOpenOrders(symbol);
        List<OrderInfo> orderInfoList = JsonParser.parseOrderInfoJson(openOrdersJson);
        List<OrderInfo> infoList = orderInfoList.stream()
                .filter(orderInfo -> "BUY".equals(orderInfo.getSide()))
                .collect(Collectors.toList());
        log.info("'{}' of symbol open orders data are parsed, successfully.", symbol);
        if (infoList.isEmpty()) {
            log.info("'{}' of symbol has 0 open orders", symbol);
            return infoList;
        }
        return this.binanceOrderService.createOrderInfos(infoList);
    }

    List<OpenOrder> cancelOpenOrders(String symbol) throws Exception {
        String openOrdersJson = this.binanceAPI.cancelOpenOrders(symbol);
        List<OpenOrder> openOrderList = JsonParser.parseOpenOrdersJson(openOrdersJson);

        log.info("'{}' of symbol cancel open orders data are parsed, successfully.", symbol);
        return this.binanceOrderService.createOpenOrders(openOrderList);
    }

    OrderResponse newOrder(String symbol, Double price, Double quantity) throws Exception {
        String orderResponseJson = this.binanceAPI.newOrder(symbol, price, quantity);
        OrderResponse orderResponse = JsonParser.parseOrderResponseJson(orderResponseJson);

        log.info("'{}' of symbol order response data are parsed, successfully.", symbol);
        return this.binanceOrderService.createOrderResponse(orderResponse);
    }

    OrderResponse newOrderWithStopLoss(String symbol, Double price, Double quantity, Double stopPrice) throws Exception {
        String orderResponseJson = this.binanceAPI.newOrderWithStopLoss(symbol, price, quantity, stopPrice);
        OrderResponse orderResponse = JsonParser.parseOrderResponseJson(orderResponseJson);

        log.info("'{}' of symbol order response data are parsed, successfully.", symbol);
        return this.binanceOrderService.createOrderResponse(orderResponse);
    }

    CancelAndNewOrderResponse cancelAndNewOrderWithStopLoss(String symbol, Double price, Double quantity, Long cancelOrderId) throws Exception {
        String orderResponseJson = this.binanceAPI.cancelAndNewOrder(symbol, price, quantity, cancelOrderId);
        CancelAndNewOrderResponse orderResponse = JsonParser.parseCancelAndNewOrderResponseJson(orderResponseJson);

        log.info("'{}' of symbol cancel and new order response data are parsed, successfully.", symbol);
        return this.binanceOrderService.createCancelAndNewOrderResponse(orderResponse);
    }

    List<AssetBalance> getUserAsset() throws Exception {
        String assetBalanceJson = this.binanceAPI.getUserAsset();
        List<AssetBalance> assetBalances = JsonParser.parseAssetBalanceJson(assetBalanceJson);

        log.info("asset balance data are parsed, successfully.");
        return this.assetBalanceService.createAssetBalances(assetBalances);
    }

    List<KlineData> getListOfKlineData(String symbol) throws Exception {
        String klinesJson = this.binanceAPI.getKlines(symbol, IntervalType.THIRTY_MINUTES);
        List<KlineData> klineDataList = JsonParser.parseKlinesJson(klinesJson);
        klineDataList.forEach(klineData -> klineData.setSymbol(symbol));
        log.info("kline data list are parsed, successfully.");
//        return this.klineDataService.createListOfKlineData(klineDataList);
        // todo does this really needed?
        return klineDataList;
    }

}

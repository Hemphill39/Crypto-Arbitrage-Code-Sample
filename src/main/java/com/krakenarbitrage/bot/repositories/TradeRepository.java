package com.krakenarbitrage.bot.repositories;

import com.krakenarbitrage.bot.domains.trade.Trade;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TradeRepository extends MongoRepository<Trade, String> {
    List<Trade> findAllByActiveEquals(Boolean active);
}

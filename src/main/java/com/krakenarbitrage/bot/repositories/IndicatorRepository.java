package com.krakenarbitrage.bot.repositories;

import com.krakenarbitrage.bot.domains.indicator.Indicator;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface IndicatorRepository extends MongoRepository<Indicator, String> {

    List<Indicator> findAllByTimestampAfterOrderByTimestampAsc(Date date);
}

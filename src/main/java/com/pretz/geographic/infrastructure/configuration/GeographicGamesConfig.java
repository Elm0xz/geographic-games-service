package com.pretz.geographic.infrastructure.configuration;

import com.pretz.geographic.application.domain.DailyEntriesService;
import com.pretz.geographic.application.domain.DailyRankingService;
import com.pretz.geographic.application.domain.service.BaseDailyRankingCalculator;
import com.pretz.geographic.application.domain.service.DailyRankingCalculator;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
import com.pretz.geographic.application.port.in.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeographicGamesConfig {

    @Bean
    AddDailyEntriesUseCase addDailyEntriesUseCase(SaveDailyEntryPort saveDailyEntryPort,
                                                  LoadGamePort loadGamePort,
                                                  LoadPlayerPort loadPlayerPort,
                                                  GameNameValidator gameNameValidator,
                                                  PlayerNameValidator playerNameValidator) {
        return new DailyEntriesService(
                saveDailyEntryPort,
                loadGamePort,
                loadPlayerPort,
                gameNameValidator,
                playerNameValidator);
    }

    @Bean
    GetDailyRankingUseCase getDailyRankingUseCase(LoadGamePort loadGamePort,
                                                  LoadDailyEntriesPort loadDailyEntriesPort,
                                                  DailyRankingCalculator dailyRankingCalculator) {
        return new DailyRankingService(
                loadGamePort,
                loadDailyEntriesPort,
                dailyRankingCalculator);
    }

    @Bean
    DailyRankingCalculator dailyRankingCalculator() {
        return new BaseDailyRankingCalculator();
    }

    @Bean
    GameNameValidator gameNameValidator() {
        return new GameNameValidator();
    }

    @Bean
    PlayerNameValidator playerNameValidator() {
        return new PlayerNameValidator();
    }
}

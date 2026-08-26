package com.pretz.geographic.infrastructure.configuration;

import com.pretz.geographic.application.domain.service.DailyEntriesService;
import com.pretz.geographic.application.domain.service.DailyRankingService;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
import com.pretz.geographic.application.domain.validation.RankingDateValidator;
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
                                                  RankingDateValidator rankingDateValidator) {
        return new DailyRankingService(
                loadGamePort,
                loadDailyEntriesPort,
                rankingDateValidator);
    }

    @Bean
    GameNameValidator gameNameValidator() {
        return new GameNameValidator();
    }

    @Bean
    PlayerNameValidator playerNameValidator() {
        return new PlayerNameValidator();
    }

    @Bean
    RankingDateValidator rankingDateValidator() {
        return new RankingDateValidator();
    }
}

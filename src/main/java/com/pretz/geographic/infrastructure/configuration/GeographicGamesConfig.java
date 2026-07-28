package com.pretz.geographic.infrastructure.configuration;

import com.pretz.geographic.application.domain.service.DailyEntriesService;
import com.pretz.geographic.application.domain.service.GameNameValidator;
import com.pretz.geographic.application.domain.service.PlayerNameValidator;
import com.pretz.geographic.application.port.in.AddDailyEntriesUseCase;
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
    GameNameValidator gameNameValidator() {
        return new GameNameValidator();
    }

    @Bean
    PlayerNameValidator playerNameValidator() {
        return new PlayerNameValidator();
    }
}

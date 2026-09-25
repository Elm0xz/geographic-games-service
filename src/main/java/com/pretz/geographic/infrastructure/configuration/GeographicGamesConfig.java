package com.pretz.geographic.infrastructure.configuration;

import com.pretz.geographic.application.domain.service.BaseWeeklyRankingCalculator;
import com.pretz.geographic.application.domain.service.DailyEntriesService;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryContextualValidationManager;
import com.pretz.geographic.application.domain.service.DailyEntryDuplicatesResolver;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryReferenceChainValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryReferenceValidationManager;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryReferenceValidator;
import com.pretz.geographic.application.domain.service.DailyRankingService;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryDateValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryGameValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryPlayerValidator;
import com.pretz.geographic.application.domain.service.WeeklyRankingCalculator;
import com.pretz.geographic.application.domain.service.WeeklyRankingService;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
import com.pretz.geographic.application.domain.validation.RankingDateValidator;
import com.pretz.geographic.application.domain.validation.WeekValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryWeekClosureValidator;
import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.application.port.in.GetWeeklyRankingUseCase;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import com.pretz.geographic.application.port.out.SaveWeeklyRankingPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class GeographicGamesConfig {

    @Bean
    AddDailyEntriesUseCase addDailyEntriesUseCase(SaveDailyEntryPort saveDailyEntryPort,
                                                  LoadGamePort loadGamePort,
                                                  LoadPlayerPort loadPlayerPort,
                                                  GameNameValidator gameNameValidator,
                                                  PlayerNameValidator playerNameValidator,
                                                  DailyEntryReferenceValidationManager dailyEntryReferenceValidationManager,
                                                  DailyEntryContextualValidationManager dailyEntryContextualValidationManager,
                                                  DailyEntryDuplicatesResolver dailyEntryDuplicatesResolver) {
        return new DailyEntriesService(
                saveDailyEntryPort,
                loadGamePort,
                loadPlayerPort,
                gameNameValidator,
                playerNameValidator,
                dailyEntryReferenceValidationManager,
                dailyEntryContextualValidationManager,
                dailyEntryDuplicatesResolver);
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
    GetWeeklyRankingUseCase getWeeklyRankingUseCase(LoadWeeklyRankingPort loadWeeklyRankingPort,
                                                    SaveWeeklyRankingPort saveWeeklyRankingPort,
                                                    LoadGamePort loadGamePort,
                                                    WeekValidator weekValidator,
                                                    WeeklyRankingCalculator weeklyRankingCalculator,
                                                    GetDailyRankingUseCase getDailyRankingUseCase) {
        return new WeeklyRankingService(
                loadWeeklyRankingPort,
                saveWeeklyRankingPort,
                loadGamePort,
                weekValidator,
                weeklyRankingCalculator,
                getDailyRankingUseCase);
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

    @Bean
    WeekValidator weekValidator() {
        return new WeekValidator();
    }

    @Bean
    WeeklyRankingCalculator weeklyRankingCalculator() {
        return new BaseWeeklyRankingCalculator();
    }

    @Bean
    DailyEntryReferenceValidationManager dailyEntryReferenceValidatorWrapper(LoadGamePort loadGamePort,
                                                                             LoadPlayerPort loadPlayerPort,
                                                                             DailyEntryReferenceValidator validator) {
        return new DailyEntryReferenceValidationManager(loadGamePort, loadPlayerPort, validator);
    }

    @Bean
    @Primary
    DailyEntryReferenceValidator dailyEntryReferenceValidator(DailyEntryDateValidator dailyEntryDateValidator,
                                                              DailyEntryGameValidator dailyEntryGameValidator,
                                                              DailyEntryPlayerValidator dailyEntryPlayerValidator) {
        return new DailyEntryReferenceChainValidator(List.of(dailyEntryDateValidator, dailyEntryGameValidator, dailyEntryPlayerValidator));
    }

    @Bean
    DailyEntryDateValidator dateValidator() {
        return new DailyEntryDateValidator();
    }

    @Bean
    DailyEntryGameValidator gameValidator() {
        return new DailyEntryGameValidator();
    }

    @Bean
    DailyEntryPlayerValidator playerValidator() {
        return new DailyEntryPlayerValidator();
    }

    @Bean
    DailyEntryContextualValidationManager dailyEntryContextualValidator(LoadWeeklyRankingPort loadWeeklyRankingPort,
                                                                        DailyEntryWeekClosureValidator dailyEntryWeekClosureValidator) {
        return new DailyEntryContextualValidationManager(loadWeeklyRankingPort, dailyEntryWeekClosureValidator);
    }

    @Bean
    DailyEntryWeekClosureValidator dailyEntryWeekClosureValidator() {
        return new DailyEntryWeekClosureValidator();
    }

    @Bean
    DailyEntryDuplicatesResolver dailyEntryDuplicatesResolver(LoadDailyEntriesPort loadDailyEntriesPort) {
        return new DailyEntryDuplicatesResolver(loadDailyEntriesPort);
    }
}

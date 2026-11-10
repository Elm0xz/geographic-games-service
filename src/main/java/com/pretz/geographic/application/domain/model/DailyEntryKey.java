package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;
import java.util.Comparator;

public record DailyEntryKey(Game game, Player player, LocalDate date) implements Comparable<DailyEntryKey> {

    private static final Comparator<DailyEntryKey> COMPARATOR = Comparator
            .comparing((DailyEntryKey key) -> key.game().gameId().id())
            .thenComparing(key -> key.player().playerId().id())
            .thenComparing(DailyEntryKey::date);

    @Override
    public int compareTo(DailyEntryKey o) {
        return COMPARATOR.compare(this, o);
    }
}

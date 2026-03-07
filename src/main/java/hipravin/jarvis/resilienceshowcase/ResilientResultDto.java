package hipravin.jarvis.resilienceshowcase;

public record ResilientResultDto(
        long callCount,
        long concurrentCount,
        String threadName) {
}

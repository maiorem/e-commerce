package com.loopers.batch.tasklet;

import com.loopers.domain.ranking.WeeklyProductRanking;
import com.loopers.domain.repository.WeeklyProductRankingRepository;
import com.loopers.infrastructure.redis.RankingRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class WeeklyRankingPersistenceTasklet implements Tasklet {

    private final RankingRedisService rankingRedisService;
    private final WeeklyProductRankingRepository weeklyProductRankingRepository;

    @Value("#{jobParameters['targetDate']}")
    private String targetDateParam;

    @Value("#{jobParameters['maxRankSize'] ?: 100}")
    private int maxRankSize;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LocalDate targetDate = LocalDate.parse(targetDateParam);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekYear = targetDate.get(weekFields.weekBasedYear());
        int weekNumber = targetDate.get(weekFields.weekOfWeekBasedYear());

        String redisKey = rankingRedisService.getWeeklyRankingKey(weekYear, weekNumber);

        log.info("주간 랭킹 영속화 시작 - {}년 {}주차, Redis Key: {}", weekYear, weekNumber, redisKey);

        // 1. 기존 랭킹 데이터 삭제
        weeklyProductRankingRepository.deleteByWeekYearAndWeekNumber(weekYear, weekNumber);

        // 2. Redis에서 TOP N 랭킹 조회
        Set<ZSetOperations.TypedTuple<String>> topRankings =
            rankingRedisService.getTopRankings(redisKey, maxRankSize);

        if (topRankings == null || topRankings.isEmpty()) {
            log.warn("Redis에서 조회된 랭킹 데이터가 없습니다 - Key: {}", redisKey);
            return RepeatStatus.FINISHED;
        }

        // 3. WeeklyProductRanking 엔티티로 변환
        List<WeeklyProductRanking> rankings = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : topRankings) {
            String productId = tuple.getValue();
            Double score = tuple.getScore();

            if (productId != null && score != null) {
                WeeklyProductRanking ranking = WeeklyProductRanking.create(
                    Long.parseLong(productId),
                    weekYear,
                    weekNumber,
                    rank,
                    score,
                    0L, // Redis에는 세부 지표가 없으므로 0으로 설정
                    0L,
                    0L,
                    0L
                );
                rankings.add(ranking);
                rank++;
            }
        }

        // 4. DB에 영속화
        weeklyProductRankingRepository.saveAllRankings(rankings);

        log.info("주간 랭킹 영속화 완료 - {}년 {}주차, 저장된 랭킹 수: {}개",
                weekYear, weekNumber, rankings.size());

        // 5. Redis 데이터 정리 (선택적 - 7일 후 만료)
        rankingRedisService.setRankingExpire(redisKey, 7, TimeUnit.DAYS);

        contribution.setExitStatus(org.springframework.batch.core.ExitStatus.COMPLETED);
        return RepeatStatus.FINISHED;
    }
}
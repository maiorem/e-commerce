package com.loopers.domain.point;

import com.loopers.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface PointRepository {
    
    PointModel save(PointModel point);
    
    Optional<PointModel> findByUserId(UserId userId);
    
    List<PointHistoryModel> findHistoryByUserId(UserId userId);
    
    PointHistoryModel saveHistory(PointHistoryModel history);
    
    void delete(PointModel point);
}

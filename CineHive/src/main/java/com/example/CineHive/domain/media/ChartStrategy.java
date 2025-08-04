<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/media/service/ChartStrategy.java
package com.example.CineHive.domain.media.service;
=======
package com.example.CineHive.domain.media;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/media/ChartStrategy.java

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.domain.common.dto.PageResponse;
import com.example.CineHive.domain.media.dto.MediaChartResponse;

@FunctionalInterface
public interface ChartStrategy {
    /**
     * 특정 차트의 데이터를 가져오는 전략을 정의합니다.
     * @param apiClient TMDB API 클라이언트
     * @param page      요청할 페이지 번호
     * @return 페이징된 MediaChartResponse
     */
    PageResponse<MediaChartResponse> fetchChart(TmdbApiClient client, int page);
}
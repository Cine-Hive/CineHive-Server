<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/meta/service/PlatformMetadataService.java
package com.example.CineHive.domain.meta.service;
=======
package com.example.CineHive.domain.meta;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/meta/PlatformMetadataService.java

import com.example.CineHive.domain.media.dto.PlatformOption;
import java.util.List;

/**
 * 플랫폼(OTT 등) 관련 메타데이터를 제공하는 서비스 인터페이스입니다.
 */
public interface PlatformMetadataService {

    /**
     * 필터링 등에 사용할 수 있는 전체 플랫폼 옵션 목록을 조회합니다.
     * @return 플랫폼 옵션 목록
     */
    List<PlatformOption> getPlatformOptions();
}
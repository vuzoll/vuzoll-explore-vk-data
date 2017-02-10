package com.github.vuzoll.explorevk.domain.exploration

import org.springframework.data.annotation.Id

class VkDatasetExploration {

    @Id
    String id

    String name

    Long startTimestamp
    String startTime

    String lastUpdateTime

    String endTime
    String timeTaken

    String status
    String message

    Integer datasetSize
}

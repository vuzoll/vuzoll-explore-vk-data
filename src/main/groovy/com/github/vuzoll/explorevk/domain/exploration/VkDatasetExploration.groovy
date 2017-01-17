package com.github.vuzoll.explorevk.domain.exploration

import com.github.vuzoll.explorevk.domain.vk.VkCountry
import org.springframework.data.annotation.Id

class VkDatasetExploration {

    @Id
    String id

    Long startTimestamp
    String startTime

    String lastUpdateTime

    String endTime
    String timeTaken

    String status
    String message

    Integer datasetSize

    List<DistributionEntry<VkCountry>> countriesDistribution
}

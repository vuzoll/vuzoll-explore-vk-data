package com.github.vuzoll.explorevk.domain.exploration

import com.github.vuzoll.explorevk.domain.vk.VkCity
import com.github.vuzoll.explorevk.domain.vk.VkCountry
import com.github.vuzoll.explorevk.domain.vk.VkFaculty
import com.github.vuzoll.explorevk.domain.vk.VkUniversity
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

    Distribution<VkCountry> countriesDistribution
    Distribution<VkCity> citiesDistribution
    Distribution<VkUniversity> universitiesDistribution
    Distribution<VkFaculty> facultiesDistribution
    Distribution<Integer> graduationYearDistribution
}

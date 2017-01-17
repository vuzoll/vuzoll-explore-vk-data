package com.github.vuzoll.explorevk.services

import com.github.vuzoll.explorevk.domain.exploration.DistributionEntry
import com.github.vuzoll.explorevk.domain.exploration.VkDatasetExploration
import com.github.vuzoll.explorevk.domain.vk.VkCountry
import com.github.vuzoll.explorevk.domain.vk.VkProfile
import com.github.vuzoll.explorevk.repository.vk.VkProfileRepository
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.RandomUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
@Slf4j
class ExploreVkService {

    @Autowired
    VkProfileRepository vkProfileRepository

    VkDatasetExploration getVkDatasetExploration() {
        log.info 'Generating vk dataset exploration...'

        VkDatasetExploration vkDatasetExploration = new VkDatasetExploration()

        log.info 'Calculating dataset size...'
        vkDatasetExploration.datasetSize = vkProfileRepository.count()

        log.info 'Calculating countries distribution...'
        Map<VkCountry, Integer> countriesDistribution = [:]

        int pageSize = 1000
        int numberOfDataPages = vkDatasetExploration.datasetSize / pageSize + 1
        (0..numberOfDataPages).each { int dataPageIndex ->
            log.info "Processing data page ${dataPageIndex} / ${numberOfDataPages}..."
            vkProfileRepository.findAll(new PageRequest(dataPageIndex, pageSize)).content.country.findAll({ it != null }).each { VkCountry vkCountry ->
                countriesDistribution.put(vkCountry, countriesDistribution.getOrDefault(vkCountry, 0) + 1)
            }
        }

        vkDatasetExploration.countriesDistribution = countriesDistribution.collect({ country, count -> new DistributionEntry<>(object: country, count: count) }).sort({ -it.count })

        return vkDatasetExploration
    }

    VkProfile getRandomVkProfile() {
        log.info 'Getting random vk profile...'

        log.info 'Calculating dataset size...'
        int datasetSize = vkProfileRepository.count()

        log.info 'Selecting random profile...'
        int randomVkProfileIndex = RandomUtils.nextInt(0, datasetSize)

        log.info "Getting profile with index ${randomVkProfileIndex} / ${datasetSize}..."
        return vkProfileRepository.findAll(new PageRequest(randomVkProfileIndex, 1)).content.first()
    }

    VkProfile getVkProfileById(String id) {
        log.info "Getting profile with id=${id}..."

        vkProfileRepository.findOne(id)
    }

    VkProfile getVkProfileByVkId(Integer vkId) {
        log.info "Getting profile with vkId=${vkId}..."

        vkProfileRepository.findOneByVkId(vkId)
    }
}

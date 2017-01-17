package com.github.vuzoll.explorevk.services

import com.github.vuzoll.explorevk.domain.exploration.DistributionEntry
import com.github.vuzoll.explorevk.domain.exploration.ExplorationStatus
import com.github.vuzoll.explorevk.domain.exploration.VkDatasetExploration
import com.github.vuzoll.explorevk.domain.vk.VkCountry
import com.github.vuzoll.explorevk.domain.vk.VkProfile
import com.github.vuzoll.explorevk.repository.exploration.VkDatasetExplorationRepository
import com.github.vuzoll.explorevk.repository.vk.VkProfileRepository
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.RandomUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
@Slf4j
class ExploreVkDatasetService {

    static final PeriodFormatter TIME_LIMIT_FORMAT = new PeriodFormatterBuilder()
            .appendHours().appendSuffix('h')
            .appendMinutes().appendSuffix('min')
            .appendSeconds().appendSuffix('sec')
            .toFormatter()

    @Autowired
    VkProfileRepository vkProfileRepository

    @Autowired
    VkDatasetExplorationRepository vkDatasetExplorationRepository

    VkDatasetExploration getVkDatasetExploration(VkDatasetExploration vkDatasetExploration) {
        try {
            log.info "ExplorationId=${vkDatasetExploration.id}: generating vk dataset exploration..."
            vkDatasetExploration.startTimestamp = System.currentTimeMillis()
            vkDatasetExploration.startTime = LocalDateTime.now().toString()
            vkDatasetExploration.lastUpdateTime = vkDatasetExploration.startTime
            vkDatasetExploration.timeTaken = '0sec'
            vkDatasetExploration = vkDatasetExplorationRepository.save vkDatasetExploration

            log.info "ExplorationId=${vkDatasetExploration.id}: calculating dataset size..."
            vkDatasetExploration.datasetSize = vkProfileRepository.count()
            vkDatasetExploration.lastUpdateTime = System.currentTimeMillis()
            vkDatasetExploration.timeTaken = toDurationString(System.currentTimeMillis() - vkDatasetExploration.startTimestamp)
            vkDatasetExplorationRepository.save vkDatasetExploration

            Map<VkCountry, Integer> countriesDistribution = [:]

            int pageSize = 100
            int numberOfDataPages = vkDatasetExploration.datasetSize / pageSize + 1

            (0..numberOfDataPages).each { int dataPageIndex ->
                log.info "ExplorationId=${vkDatasetExploration.id}: processing data page ${dataPageIndex} / ${numberOfDataPages}..."
                vkProfileRepository.findAll(new PageRequest(dataPageIndex, pageSize)).content.country.findAll({ it != null }).each { VkCountry vkCountry ->
                    countriesDistribution.put(vkCountry, countriesDistribution.getOrDefault(vkCountry, 0) + 1)
                }

                vkDatasetExploration.countriesDistribution = countriesDistribution.collect({ country, count -> new DistributionEntry<>(object: country, count: count) }).sort({ -it.count })
                vkDatasetExploration.lastUpdateTime = System.currentTimeMillis()
                vkDatasetExploration.timeTaken = toDurationString(System.currentTimeMillis() - vkDatasetExploration.startTimestamp)
                vkDatasetExplorationRepository.save vkDatasetExploration
            }

            vkDatasetExploration.countriesDistribution = countriesDistribution.collect({ country, count -> new DistributionEntry<>(object: country, count: count) }).sort({ -it.count })

            return vkDatasetExploration
        } catch (e) {
            log.error("ExplorationId=${vkDatasetExploration.id}: exploration failed", e)

            vkDatasetExploration.message = "Failed because of ${e.class.name}, with message: ${e.message}"
            vkDatasetExploration.lastUpdateTime = System.currentTimeMillis()
            vkDatasetExploration.timeTaken = toDurationString(System.currentTimeMillis() - vkDatasetExploration.startTimestamp)
            vkDatasetExploration.status = ExplorationStatus.FAILED.toString()
            vkDatasetExplorationRepository.save vkDatasetExploration

            throw e
        }
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

    static String toDurationString(long duration) {
        String durationString = TIME_LIMIT_FORMAT.print(new Period(duration))
        if (durationString.empty) {
            return '0sec'
        } else {
            return durationString
        }
    }
}

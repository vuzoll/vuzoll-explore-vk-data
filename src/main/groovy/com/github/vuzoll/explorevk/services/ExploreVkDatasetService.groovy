package com.github.vuzoll.explorevk.services

import com.github.vuzoll.explorevk.domain.exploration.Distribution
import com.github.vuzoll.explorevk.domain.exploration.ExplorationStatus
import com.github.vuzoll.explorevk.domain.exploration.VkDatasetExploration
import com.github.vuzoll.explorevk.domain.vk.VkFaculty
import com.github.vuzoll.explorevk.domain.vk.VkProfile
import com.github.vuzoll.explorevk.domain.vk.VkUniversity
import com.github.vuzoll.explorevk.domain.vk.VkUniversityRecord
import com.github.vuzoll.explorevk.repository.exploration.VkDatasetExplorationRepository
import com.github.vuzoll.explorevk.repository.vk.VkProfileRepository
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.RandomUtils
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
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
    MongoTemplate mongoTemplate

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

            vkDatasetExploration.countriesDistribution = new Distribution<>()
            vkDatasetExploration.citiesDistribution = new Distribution<>()
            vkDatasetExploration.universitiesDistribution = new Distribution<>()
            vkDatasetExploration.facultiesDistribution = new Distribution<>()
            vkDatasetExploration.graduationYearDistribution = new Distribution<>()

            mongoTemplate.stream('{}', VkProfile).eachWithIndex { VkProfile vkProfile, int index ->
                if (index % 100 == 0) {
                    log.info "ExplorationId=${vkDatasetExploration.id}: processing record ${index} / ${vkDatasetExploration.datasetSize}..."

                    vkDatasetExploration.lastUpdateTime = System.currentTimeMillis()
                    vkDatasetExploration.timeTaken = toDurationString(System.currentTimeMillis() - vkDatasetExploration.startTimestamp)
                    vkDatasetExplorationRepository.save vkDatasetExploration
                }

                vkDatasetExploration.countriesDistribution.add(vkProfile.country)
                vkDatasetExploration.citiesDistribution.add(vkProfile.city)
                vkDatasetExploration.universitiesDistribution.add(vkProfile.universityRecords.collect(this.&toUniversity))
                vkDatasetExploration.facultiesDistribution.add(vkProfile.universityRecords.collect(this.&toFaculty))
                vkDatasetExploration.graduationYearDistribution.add(vkProfile.universityRecords.graduationYear)
            }

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

    private VkUniversity toUniversity(VkUniversityRecord vkUniversityRecord) {
        if (vkUniversityRecord.universityId == null) {
            return null
        }
        if (vkUniversityRecord.universityName == null) {
            return null
        }
        if (vkUniversityRecord.countryId == null) {
            return null
        }

        return VkUniversity.builder()
                .universityId(vkUniversityRecord.universityId)
                .universityName(vkUniversityRecord.universityName)
                .countryId(vkUniversityRecord.countryId)
                .cityId(vkUniversityRecord.cityId)
                .build()
    }

    private VkFaculty toFaculty(VkUniversityRecord vkUniversityRecord) {
        VkUniversity university = toUniversity(vkUniversityRecord)
        if (university == null) {
            return null
        }
        if (vkUniversityRecord.facultyId == null) {
            return null
        }
        if (vkUniversityRecord.facultyName == null) {
            return null
        }

        return VkFaculty.builder()
                .university(university)
                .facultyId(vkUniversityRecord.facultyId)
                .facultyName(vkUniversityRecord.facultyName)
                .build()
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

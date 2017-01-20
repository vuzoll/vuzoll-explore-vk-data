package com.github.vuzoll.explorevk.services

import com.github.vuzoll.explorevk.domain.exploration.CurrentLocationExploration
import com.github.vuzoll.explorevk.domain.exploration.Distribution
import com.github.vuzoll.explorevk.domain.exploration.ExplorationStatus
import com.github.vuzoll.explorevk.domain.exploration.TopFacultiesExploration
import com.github.vuzoll.explorevk.domain.exploration.VkDatasetExploration
import com.github.vuzoll.explorevk.domain.vk.VkFaculty
import com.github.vuzoll.explorevk.domain.vk.VkProfile
import com.github.vuzoll.explorevk.domain.vk.VkUniversity
import com.github.vuzoll.explorevk.domain.vk.VkUniversityRecord
import com.github.vuzoll.explorevk.repository.exploration.VkDatasetExplorationRepository
import com.github.vuzoll.explorevk.repository.vk.VkProfileRepository
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
@Slf4j
class ExploreVkDatasetService {

    static Integer EXPLORATION_CHUNK_SIZE = System.getenv('EXPLORE_VK_EXPLORATION_CHUNK_SIZE') ? Integer.parseInt(System.getenv('EXPLORE_VK_EXPLORATION_CHUNK_SIZE')) : 100

    static final PeriodFormatter TIME_LIMIT_FORMAT = new PeriodFormatterBuilder()
            .appendHours().appendSuffix('h')
            .appendMinutes().appendSuffix('min')
            .appendSeconds().appendSuffix('sec')
            .toFormatter()

    static final Integer UKRAINE_ID = 2

    @Autowired
    VkProfileRepository vkProfileRepository

    @Autowired
    MongoTemplate mongoTemplate

    @Autowired
    VkDatasetExplorationRepository vkDatasetExplorationRepository

    VkDatasetExploration exploreCurrentLocation(CurrentLocationExploration currentLocationExploration) {
        explore(currentLocationExploration,
                { CurrentLocationExploration exploration ->
                    exploration.countryDistribution = new Distribution<>()
                    exploration.cityDistribution = new Distribution<>()
                },
                { CurrentLocationExploration exploration, VkProfile vkProfile ->
                    exploration.countryDistribution.add(vkProfile.country)
                    exploration.cityDistribution.add(vkProfile.city)
                })
    }

    VkDatasetExploration exploreTopFaculties(TopFacultiesExploration topFacultiesExploration) {
        explore(topFacultiesExploration,
                { TopFacultiesExploration exploration ->
                    exploration.facultyDistribution = new Distribution<>({ VkFaculty it -> it == null || it.university.countryId != UKRAINE_ID }, topFacultiesExploration.numberOfFacultiesToTake)
                },
                { TopFacultiesExploration exploration, VkProfile vkProfile ->
                    exploration.facultyDistribution.add(vkProfile.universityRecords.collect(this.&toFaculty))
                })
    }

    private VkDatasetExploration explore(VkDatasetExploration vkDatasetExploration, Closure initAction, Closure exploreAction) {
        try {
            log.info "ExplorationId=${vkDatasetExploration.id}: generating vk dataset exploration..."
            vkDatasetExploration.startTimestamp = System.currentTimeMillis()
            vkDatasetExploration.startTime = LocalDateTime.now().toString()
            vkDatasetExploration.lastUpdateTime = vkDatasetExploration.startTime
            vkDatasetExploration.timeTaken = '0sec'
            vkDatasetExploration = vkDatasetExplorationRepository.save vkDatasetExploration

            log.info "ExplorationId=${vkDatasetExploration.id}: calculating dataset size..."
            vkDatasetExploration.datasetSize = vkProfileRepository.count()
            vkDatasetExploration.lastUpdateTime = LocalDateTime.now().toString()
            vkDatasetExploration.timeTaken = toDurationString(System.currentTimeMillis() - vkDatasetExploration.startTimestamp)
            vkDatasetExplorationRepository.save vkDatasetExploration

            initAction.call(vkDatasetExploration)
            vkDatasetExplorationRepository.save vkDatasetExploration

            mongoTemplate.stream(new Query(), VkProfile).eachWithIndex { VkProfile vkProfile, int index ->
                if (index % EXPLORATION_CHUNK_SIZE == 0) {
                    log.info "ExplorationId=${vkDatasetExploration.id}: processing record ${index} / ${vkDatasetExploration.datasetSize}..."

                    vkDatasetExploration.lastUpdateTime = LocalDateTime.now().toString()
                    vkDatasetExploration.timeTaken = toDurationString(System.currentTimeMillis() - vkDatasetExploration.startTimestamp)
                    vkDatasetExplorationRepository.save vkDatasetExploration
                }

                exploreAction.call(vkDatasetExploration, vkProfile)
            }

            log.info "ExplorationId=${vkDatasetExploration.id}: exploration succeeded"
            vkDatasetExploration.endTime = LocalDateTime.now().toString()
            vkDatasetExploration.lastUpdateTime = vkDatasetExploration.endTime
            vkDatasetExploration.timeTaken = toDurationString(System.currentTimeMillis() - vkDatasetExploration.startTimestamp)
            vkDatasetExploration.status = ExplorationStatus.COMPLETED.toString()
            vkDatasetExplorationRepository.save vkDatasetExploration

            return vkDatasetExploration
        } catch (e) {
            log.error("ExplorationId=${vkDatasetExploration.id}: exploration failed", e)
            vkDatasetExploration.message = "Failed because of ${e.class.name}, with message: ${e.message}"
            vkDatasetExploration.endTime = LocalDateTime.now().toString()
            vkDatasetExploration.lastUpdateTime = vkDatasetExploration.endTime
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
        if (StringUtils.isBlank(vkUniversityRecord.universityName)) {
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
        if (StringUtils.isBlank(vkUniversityRecord.facultyName)) {
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
